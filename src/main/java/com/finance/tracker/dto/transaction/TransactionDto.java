package com.finance.tracker.dto.transaction;

import com.finance.tracker.entity.Transaction.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Транзакция (доход или расход)")
public class TransactionDto {
    private String id;

    @NotBlank
    @Schema(example = "Продукты в Пятёрочке")
    private String title;

    @Schema(example = "Супермаркет Пятёрочка")
    private String description;

    @NotNull @DecimalMin("0.01")
    @Schema(example = "1500.00")
    private BigDecimal amount;

    @NotBlank
    @Schema(example = "products", description = "Slug категории")
    private String category;

    @NotBlank
    @Schema(example = "2026-03-01", description = "Дата в формате YYYY-MM-DD")
    private String date;

    @Schema(example = "14:30", description = "Время в формате HH:mm")
    private String time;

    @NotNull
    @Schema(example = "EXPENSE")
    private TransactionType type;
}
