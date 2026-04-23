package com.finance.tracker.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AiInsightDto {
    private String id;
    private String type;      // DANGER | SUCCESS | WARNING | INFO
    private String title;
    private String description;
    private String recommendation;
}
