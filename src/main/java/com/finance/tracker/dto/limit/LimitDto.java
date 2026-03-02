package com.finance.tracker.dto.limit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Лимит расходов по категории")
public class LimitDto {
    private String id;
    private String categorySlug;
    private String categoryName;

    @Schema(example = "Март 2026")
    private String period;

    private BigDecimal limitAmount;
    private BigDecimal spentAmount;
}
