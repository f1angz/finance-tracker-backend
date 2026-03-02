package com.finance.tracker.dto.goal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Финансовая цель")
public class GoalDto {
    private String id;
    private String emoji;
    private String title;
    private int daysLeft;
    private BigDecimal savedAmount;
    private BigDecimal targetAmount;

    @Schema(example = "3B82F6")
    private String accentColor;
}
