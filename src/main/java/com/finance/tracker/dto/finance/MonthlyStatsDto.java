package com.finance.tracker.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class MonthlyStatsDto {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
}
