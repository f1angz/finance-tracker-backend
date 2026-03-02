package com.finance.tracker.controller;

import com.finance.tracker.dto.transaction.TransactionDto;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Управление транзакциями")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Получить список транзакций")
    public List<TransactionDto> getTransactions(
            @Parameter(description = "INCOME | EXPENSE") @RequestParam(required = false) String type,
            @Parameter(description = "Строка поиска")    @RequestParam(required = false) String search,
            @Parameter(description = "Номер страницы")  @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Записей на стр.") @RequestParam(defaultValue = "50") int limit
    ) {
        return transactionService.getTransactions(securityUtils.currentUserId(), type, search, page, limit);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать транзакцию")
    public TransactionDto createTransaction(@Valid @RequestBody TransactionDto dto) {
        return transactionService.createTransaction(securityUtils.currentUserId(), dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить транзакцию")
    public void deleteTransaction(@PathVariable String id) {
        transactionService.deleteTransaction(securityUtils.currentUserId(), id);
    }
}
