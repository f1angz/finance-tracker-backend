package com.finance.tracker.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.FontMappers;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BankStatementParser {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yy");

    // Detects start of a transaction line: DD.MM.YY [HH:MM] DD.MM.YY ...
    private static final Pattern TX_HEADER = Pattern.compile(
            "^(\\d{2}\\.\\d{2}\\.\\d{2})(?:\\s+\\d{2}:\\d{2})?\\s+\\d{2}\\.\\d{2}\\.\\d{2}\\s+"
    );

    // Matches amounts like "2 800.00 ₽" or "+ 44 700.00 ₽"
    private static final Pattern AMOUNT_PAT = Pattern.compile(
            "(\\+\\s*)?(\\d[\\d\\s]*\\.\\d{2})\\s*₽"
    );

    // Lines to skip (section headers, column headers, etc.)
    private static final Pattern SKIP_LINE = Pattern.compile(
            "^(Дата|Расходы:|Поступления:|Операции по|Баланс|Кэшбэк\\s|Выписка|Сумма|Описание|СЕМАГИН|\\d{2}\\.\\d{2}\\.\\d{4}).*",
            Pattern.CASE_INSENSITIVE
    );

    @PostConstruct
    void initFontMapper() {
        try {
            FontMappers.instance();
        } catch (Error e) {
            log.warn("PDFBox font scanner failed (likely corrupted system font): {}. " +
                    "Text extraction may be affected.", e.getMessage());
        }
    }

    public record RawTransaction(LocalDate date, String description, BigDecimal amount, boolean income) {}

    public List<RawTransaction> parse(InputStream pdfStream) throws IOException {
        byte[] bytes = pdfStream.readAllBytes();
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            String text = stripper.getText(doc);
            // Normalize non-breaking spaces
            text = text.replace('\u00A0', ' ');
            return parseTransactions(text);
        }
    }

    private List<RawTransaction> parseTransactions(String text) {
        String[] lines = text.split("\\r?\\n");
        List<RawTransaction> result = new ArrayList<>();

        String currentDate = null;
        StringBuilder currentBlock = null;

        for (String rawLine : lines) {
            String line = rawLine.strip();
            if (line.isEmpty() || SKIP_LINE.matcher(line).matches()) continue;

            Matcher headerMatcher = TX_HEADER.matcher(line);
            if (headerMatcher.find()) {
                // Flush previous block
                if (currentDate != null && currentBlock != null) {
                    parseBlock(currentDate, currentBlock.toString().strip(), result);
                }
                currentDate = headerMatcher.group(1);
                currentBlock = new StringBuilder(line.substring(headerMatcher.end()));
            } else if (currentBlock != null) {
                // Continuation line (e.g., phone number or wrapped description)
                currentBlock.append(" ").append(line);
            }
        }
        // Flush last block
        if (currentDate != null && currentBlock != null) {
            parseBlock(currentDate, currentBlock.toString().strip(), result);
        }

        log.info("Parsed {} transactions from bank statement", result.size());
        return result;
    }

    private void parseBlock(String dateStr, String block, List<RawTransaction> result) {
        List<MatchResult> amounts = AMOUNT_PAT.matcher(block)
                .results()
                .collect(Collectors.toList());

        if (amounts.size() < 1) return;

        // There are usually 2 identical amounts at the end (operation + account currency)
        // Take the last two (or one if only one found)
        MatchResult firstOfLast = amounts.size() >= 2
                ? amounts.get(amounts.size() - 2)
                : amounts.get(amounts.size() - 1);

        String description = block.substring(0, firstOfLast.start()).strip();
        if (description.isBlank()) return;

        boolean isIncome = firstOfLast.group(1) != null;
        String amountStr = firstOfLast.group(2).replaceAll("\\s+", "");

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FMT);
            BigDecimal amount = new BigDecimal(amountStr);
            result.add(new RawTransaction(date, description, amount, isIncome));
        } catch (Exception e) {
            log.warn("Skipping malformed transaction: date={} block={}", dateStr, block);
        }
    }
}
