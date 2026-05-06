package com.finance.tracker.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class CategoryExpenseDto {
    private String category;
    private String categoryName;
    private BigDecimal amount;
    private float percentage;
}
