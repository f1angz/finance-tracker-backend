package com.finance.tracker.dto.debt;

import com.finance.tracker.entity.Debt.DebtType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDebtRequest {
    @NotBlank
    private String personName;

    @NotNull
    private DebtType type;

    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    private String dueDate;
}
