package com.finance.tracker.controller;

import com.finance.tracker.dto.limit.LimitDto;
import com.finance.tracker.dto.limit.LimitRequest;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.LimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/limits")
@RequiredArgsConstructor
@Tag(name = "Limits", description = "Лимиты расходов по категориям")
public class LimitController {

    private final LimitService limitService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Получить лимиты за период")
    public List<LimitDto> getLimits(
            @Parameter(description = "Период в формате YYYY-MM") @RequestParam(required = false) String period
    ) {
        return limitService.getLimits(securityUtils.currentUserId(), period);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать лимит")
    public LimitDto createLimit(@Valid @RequestBody LimitRequest request) {
        return limitService.createLimit(securityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить лимит")
    public LimitDto updateLimit(@PathVariable String id, @Valid @RequestBody LimitRequest request) {
        return limitService.updateLimit(securityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить лимит")
    public void deleteLimit(@PathVariable String id) {
        limitService.deleteLimit(securityUtils.currentUserId(), id);
    }
}
