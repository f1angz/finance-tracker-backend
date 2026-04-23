package com.finance.tracker.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AiHealthScoreDto {
    private int overall;
    private int expenses;
    private int savings;
    private int goals;
    private int discipline;
}
