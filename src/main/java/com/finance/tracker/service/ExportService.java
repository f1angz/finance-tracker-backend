package com.finance.tracker.service;

import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.Transaction.TransactionType;
import com.finance.tracker.repository.TransactionRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final Color COLOR_PRIMARY   = new Color(30, 64, 175);
    private static final Color COLOR_INCOME    = new Color(22, 163, 74);
    private static final Color COLOR_EXPENSE   = new Color(220, 38, 38);
    private static final Color COLOR_ROW_EVEN  = new Color(249, 250, 251);
    private static final Color COLOR_HEADER_BG = new Color(30, 64, 175);
    private static final Color COLOR_LIGHT_GRAY = new Color(156, 163, 175);

    public byte[] exportPdf(UUID userId, LocalDate from, LocalDate to) throws Exception {
        List<Transaction> transactions = transactionRepository.findAllByUserIdOrderByDateDescCreatedAtDesc(userId);

        // Filter by date range
        List<Transaction> filtered = transactions.stream()
                .filter(t -> !t.getDate().isBefore(from) && !t.getDate().isAfter(to))
                .sorted((a, b) -> b.getDate().compareTo(a.getDate()))
                .toList();

        BigDecimal totalIncome = filtered.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = filtered.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal balance = totalIncome.subtract(totalExpense);

        BaseFont bf = loadFont();
        Font fontTitle    = new Font(bf, 22, Font.BOLD, Color.WHITE);
        Font fontSubtitle = new Font(bf, 10, Font.NORMAL, new Color(209, 213, 219));
        Font fontSection  = new Font(bf, 13, Font.BOLD, COLOR_PRIMARY);
        Font fontNormal   = new Font(bf, 9,  Font.NORMAL, new Color(31, 41, 55));
        Font fontBold     = new Font(bf, 9,  Font.BOLD,   new Color(31, 41, 55));
        Font fontIncome   = new Font(bf, 9,  Font.BOLD,   COLOR_INCOME);
        Font fontExpense  = new Font(bf, 9,  Font.BOLD,   COLOR_EXPENSE);
        Font fontSmall    = new Font(bf, 8,  Font.NORMAL, COLOR_LIGHT_GRAY);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(doc, baos);
        doc.open();

        // ── Header ─────────────────────────────────────────────────────────────
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(COLOR_PRIMARY);
        headerCell.setPadding(20);
        headerCell.setBorder(Rectangle.NO_BORDER);
        headerCell.addElement(new Paragraph("Финансовый отчёт", fontTitle));
        headerCell.addElement(new Paragraph(
                "Период: " + from.format(DISPLAY_FMT) + " — " + to.format(DISPLAY_FMT), fontSubtitle));
        headerCell.addElement(new Paragraph(
                "Сформирован: " + LocalDate.now().format(DISPLAY_FMT), fontSmall));
        header.addCell(headerCell);
        doc.add(header);
        doc.add(Chunk.NEWLINE);

        // ── Summary ────────────────────────────────────────────────────────────
        doc.add(new Paragraph("Сводка", fontSection));
        doc.add(Chunk.NEWLINE);

        PdfPTable summary = new PdfPTable(3);
        summary.setWidthPercentage(100);
        summary.setSpacingBefore(4);

        addSummaryCell(summary, bf, "Доходы", formatAmount(totalIncome), COLOR_INCOME);
        addSummaryCell(summary, bf, "Расходы", formatAmount(totalExpense), COLOR_EXPENSE);
        addSummaryCell(summary, bf, "Баланс", formatAmount(balance),
                balance.compareTo(BigDecimal.ZERO) >= 0 ? COLOR_INCOME : COLOR_EXPENSE);

        doc.add(summary);
        doc.add(Chunk.NEWLINE);

        // ── Transactions table ─────────────────────────────────────────────────
        doc.add(new Paragraph("Транзакции (" + filtered.size() + ")", fontSection));
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 4f, 2f, 2f});
        table.setSpacingBefore(4);

        // Table header
        for (String col : new String[]{"Дата", "Описание", "Категория", "Сумма"}) {
            PdfPCell cell = new PdfPCell(new Phrase(col, new Font(bf, 9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setPadding(7);
            cell.setBorderColor(COLOR_HEADER_BG);
            table.addCell(cell);
        }

        // Table rows
        boolean even = false;
        for (Transaction tx : filtered) {
            Color rowBg = even ? COLOR_ROW_EVEN : Color.WHITE;
            even = !even;

            boolean isIncome = tx.getType() == TransactionType.INCOME;
            Font amountFont = isIncome ? fontIncome : fontExpense;
            String amountStr = (isIncome ? "+ " : "- ") + formatAmount(tx.getAmount());

            addTxCell(table, tx.getDate().format(DISPLAY_FMT), fontNormal, rowBg);
            addTxCell(table, tx.getTitle(), fontNormal, rowBg);
            addTxCell(table, tx.getCategorySlug(), fontSmall, rowBg);
            addTxCell(table, amountStr, amountFont, rowBg);
        }

        doc.add(table);

        // ── Footer ─────────────────────────────────────────────────────────────
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Finance Tracker — автоматически сформированный отчёт", fontSmall);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);

        doc.close();
        return baos.toByteArray();
    }

    private void addSummaryCell(PdfPTable table, BaseFont bf, String label, String value, Color valueColor) {
        Font labelFont = new Font(bf, 9, Font.NORMAL, new Color(107, 114, 128));
        Font valueFont = new Font(bf, 16, Font.BOLD, valueColor);

        PdfPCell cell = new PdfPCell();
        cell.setPadding(12);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value, valueFont));
        table.addCell(cell);
    }

    private void addTxCell(PdfPTable table, String text, Font font, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(6);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new Color(229, 231, 235));
        table.addCell(cell);
    }

    private String formatAmount(BigDecimal amount) {
        return String.format("%,.2f ₽", amount.abs()).replace(',', ' ');
    }

    private BaseFont loadFont() throws Exception {
        String[] paths = {
                System.getenv("WINDIR") + "\\Fonts\\arial.ttf",
                System.getenv("WINDIR") + "\\Fonts\\segoeui.ttf",
                "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
                "/Library/Fonts/Arial.ttf"
        };
        for (String path : paths) {
            try {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {}
        }
        // Last resort — no Cyrillic but at least won't crash
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
    }
}
