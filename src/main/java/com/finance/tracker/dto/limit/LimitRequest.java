package com.finance.tracker.dto.limit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос для создания / обновления лимита")
public class LimitRequest {

    @NotBlank
    @Schema(example = "products")
    private String categorySlug;

    @NotNull @DecimalMin("0.01")
    @Schema(example = "10000.00")
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "period must be in format YYYY-MM")
    @Schema(example = "2026-03")
    private String period;
}
