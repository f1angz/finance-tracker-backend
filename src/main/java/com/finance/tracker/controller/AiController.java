package com.finance.tracker.controller;

import com.finance.tracker.dto.ai.*;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI Assistant", description = "ИИ-помощник по финансам")
public class AiController {

    private final AiService aiService;
    private final SecurityUtils securityUtils;

    @GetMapping("/health-score")
    @Operation(summary = "Финансовое здоровье пользователя")
    public AiHealthScoreDto getHealthScore() {
        return aiService.getHealthScore(securityUtils.currentUserId());
    }

    @GetMapping("/insights")
    @Operation(summary = "Аналитические выводы")
    public List<AiInsightDto> getInsights() {
        return aiService.getInsights(securityUtils.currentUserId());
    }

    @GetMapping("/tips")
    @Operation(summary = "Финансовые советы")
    public List<AiTipDto> getTips() {
        return aiService.getTips(securityUtils.currentUserId());
    }

    @PostMapping("/chat")
    @Operation(summary = "Чат с ИИ-ассистентом")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return aiService.chat(securityUtils.currentUserId(), request);
    }
}
