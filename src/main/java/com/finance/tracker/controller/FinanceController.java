package com.finance.tracker.controller;

import com.finance.tracker.dto.finance.CategoryExpenseDto;
import com.finance.tracker.dto.finance.FinanceStatsDto;
import com.finance.tracker.dto.finance.MonthlyStatsDto;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/finance")
@RequiredArgsConstructor
@Tag(name = "Finance", description = "Финансовая статистика (главный экран)")
public class FinanceController {

    private final FinanceService financeService;
    private final SecurityUtils securityUtils;

    @GetMapping("/stats")
    @Operation(summary = "Общая статистика за месяц")
    public FinanceStatsDto getStats(
            @Parameter(description = "Месяц в формате yyyy-MM, по умолчанию — текущий")
            @RequestParam Optional<YearMonth> month
    ) {
        return financeService.getStats(securityUtils.currentUserId(), month.orElse(YearMonth.now()));
    }

    @GetMapping("/category-expenses")
    @Operation(summary = "Расходы по категориям за месяц")
    public List<CategoryExpenseDto> getCategoryExpenses(
            @Parameter(description = "Месяц в формате yyyy-MM, по умолчанию — текущий")
            @RequestParam Optional<YearMonth> month
    ) {
        return financeService.getCategoryExpenses(securityUtils.currentUserId(), month.orElse(YearMonth.now()));
    }

    @GetMapping("/monthly-stats")
    @Operation(summary = "Статистика по месяцам (последние 6 месяцев)")
    public List<MonthlyStatsDto> getMonthlyStats() {
        return financeService.getMonthlyStats(securityUtils.currentUserId());
    }
}
