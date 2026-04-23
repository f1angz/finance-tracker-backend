package com.finance.tracker.service;

import com.finance.tracker.dto.finance.CategoryExpenseDto;
import com.finance.tracker.dto.finance.FinanceStatsDto;
import com.finance.tracker.dto.finance.MonthlyStatsDto;
import com.finance.tracker.entity.Transaction.TransactionType;
import com.finance.tracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MMM", new Locale("ru"));

    public FinanceStatsDto getStats(UUID userId, YearMonth current) {
        YearMonth previous = current.minusMonths(1);

        BigDecimal curIncome  = sumForPeriod(userId, TransactionType.INCOME,  current);
        BigDecimal curExpense = sumForPeriod(userId, TransactionType.EXPENSE, current);
        BigDecimal prevIncome  = sumForPeriod(userId, TransactionType.INCOME,  previous);
        BigDecimal prevExpense = sumForPeriod(userId, TransactionType.EXPENSE, previous);

        BigDecimal balance  = curIncome.subtract(curExpense);
        BigDecimal savings  = curIncome.compareTo(curExpense) > 0 ? curIncome.subtract(curExpense) : BigDecimal.ZERO;

        return new FinanceStatsDto(
                balance,   pctChange(curIncome.subtract(curExpense), prevIncome.subtract(prevExpense)),
                curIncome, pctChange(curIncome, prevIncome),
                curExpense, pctChange(curExpense, prevExpense),
                savings,   pctChange(savings, prevIncome.subtract(prevExpense).max(BigDecimal.ZERO))
        );
    }

    public List<CategoryExpenseDto> getCategoryExpenses(UUID userId, YearMonth current) {
        LocalDate from = current.atDay(1);
        LocalDate to   = current.atEndOfMonth();

        List<Object[]> rows = transactionRepository.sumExpensesGroupedByCategory(userId, from, to);
        BigDecimal total = rows.stream()
                .map(r -> (BigDecimal) r[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryExpenseDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            String slug = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            float pct = total.compareTo(BigDecimal.ZERO) == 0 ? 0f :
                    amount.divide(total, 4, RoundingMode.HALF_UP).floatValue() * 100f;
            result.add(new CategoryExpenseDto(slug, amount, pct));
        }
        return result;
    }

    public List<MonthlyStatsDto> getMonthlyStats(UUID userId) {
        List<MonthlyStatsDto> result = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            BigDecimal income  = sumForPeriod(userId, TransactionType.INCOME,  ym);
            BigDecimal expense = sumForPeriod(userId, TransactionType.EXPENSE, ym);
            result.add(new MonthlyStatsDto(ym.format(MONTH_FMT), income, expense));
        }
        return result;
    }

    private BigDecimal sumForPeriod(UUID userId, TransactionType type, YearMonth ym) {
        return transactionRepository.sumByUserIdAndTypeAndDateBetween(
                userId, type, ym.atDay(1), ym.atEndOfMonth());
    }

    private double pctChange(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .doubleValue() * 100.0;
    }
}
