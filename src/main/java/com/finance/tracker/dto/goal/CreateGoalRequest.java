package com.finance.tracker.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос для создания новой цели")
public class CreateGoalRequest {

    @NotBlank
    @Schema(example = "🚗")
    private String emoji;

    @NotBlank
    @Schema(example = "Новый автомобиль")
    private String title;

    @NotNull @DecimalMin("0.01")
    @Schema(example = "500000.00")
    private BigDecimal targetAmount;

    @NotBlank
    @Schema(example = "2026-12-31")
    private String targetDate;

    @Schema(example = "3B82F6")
    private String accentColor;
}
