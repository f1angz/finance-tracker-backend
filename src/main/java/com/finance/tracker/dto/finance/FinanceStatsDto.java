package com.finance.tracker.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class FinanceStatsDto {
    private BigDecimal balance;
    private double balanceChange;
    private BigDecimal income;
    private double incomeChange;
    private BigDecimal expense;
    private double expenseChange;
    private BigDecimal savings;
    private double savingsChange;
}
