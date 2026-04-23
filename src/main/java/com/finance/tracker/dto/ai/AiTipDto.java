package com.finance.tracker.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class AiTipDto {
    private String id;
    private String category;
    private String title;
    private String effect;
}
