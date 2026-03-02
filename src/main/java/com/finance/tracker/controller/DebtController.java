package com.finance.tracker.controller;

import com.finance.tracker.dto.debt.CreateDebtRequest;
import com.finance.tracker.dto.debt.DebtDto;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/debts")
@RequiredArgsConstructor
@Tag(name = "Debts", description = "Долговые обязательства")
public class DebtController {

    private final DebtService debtService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Получить список долгов")
    public List<DebtDto> getDebts() {
        return debtService.getDebts(securityUtils.currentUserId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать долг")
    public DebtDto createDebt(@Valid @RequestBody CreateDebtRequest request) {
        return debtService.createDebt(securityUtils.currentUserId(), request);
    }

    @PutMapping("/{id}/paid")
    @Operation(summary = "Отметить долг как погашенный")
    public DebtDto markAsPaid(@PathVariable String id) {
        return debtService.markAsPaid(securityUtils.currentUserId(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить долг")
    public void deleteDebt(@PathVariable String id) {
        debtService.deleteDebt(securityUtils.currentUserId(), id);
    }
}
