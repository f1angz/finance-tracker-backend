package com.finance.tracker.dto.goal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoalContributionRequest {
    @NotNull @DecimalMin("0.01")
    private BigDecimal amount;
}
