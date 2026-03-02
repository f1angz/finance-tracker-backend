package com.finance.tracker.dto.debt;

import com.finance.tracker.entity.Debt.DebtType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Долговое обязательство")
public class DebtDto {
    private String id;
    private String personName;
    private DebtType type;
    private BigDecimal amount;

    @Schema(example = "2026-06-01")
    private String dueDate;

    private Boolean isPaid;
}
