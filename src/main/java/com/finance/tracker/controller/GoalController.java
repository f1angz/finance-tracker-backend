package com.finance.tracker.controller;

import com.finance.tracker.dto.goal.CreateGoalRequest;
import com.finance.tracker.dto.goal.GoalContributionRequest;
import com.finance.tracker.dto.goal.GoalDto;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Финансовые цели")
public class GoalController {

    private final GoalService goalService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Получить список целей")
    public List<GoalDto> getGoals() {
        return goalService.getGoals(securityUtils.currentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать цель")
    public GoalDto createGoal(@Valid @RequestBody CreateGoalRequest request) {
        return goalService.createGoal(securityUtils.currentUserId(), request);
    }

    @PostMapping("/{id}/contributions")
    @Operation(summary = "Пополнить цель")
    public GoalDto addContribution(
            @PathVariable String id,
            @Valid @RequestBody GoalContributionRequest request
    ) {
        return goalService.addContribution(securityUtils.currentUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить цель")
    public void deleteGoal(@PathVariable String id) {
        goalService.deleteGoal(securityUtils.currentUserId(), id);
    }
}
