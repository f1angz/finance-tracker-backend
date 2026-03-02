package com.finance.tracker.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class AiInsightDto {
    private String id;
    private String type;      // DANGER | SUCCESS | WARNING | INFO
    private String title;
    private String description;
    private String recommendation;
}
