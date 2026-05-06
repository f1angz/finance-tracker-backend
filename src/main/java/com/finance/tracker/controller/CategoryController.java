package com.finance.tracker.controller;

import com.finance.tracker.dto.category.CategoryDto;
import com.finance.tracker.security.SecurityUtils;
import com.finance.tracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Управление категориями")
public class CategoryController {

    private final CategoryService categoryService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "Получить список категорий")
    public List<CategoryDto> getCategories(
            @Parameter(description = "EXPENSE | INCOME | OTHER") @RequestParam(required = false) String type
    ) {
        return categoryService.getCategories(securityUtils.currentUserId(), type);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать категорию")
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto dto) {
        return categoryService.createCategory(securityUtils.currentUserId(), dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить категорию")
    public CategoryDto updateCategory(@PathVariable String id, @Valid @RequestBody CategoryDto dto) {
        return categoryService.updateCategory(securityUtils.currentUserId(), id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить категорию")
    public void deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(securityUtils.currentUserId(), id);
    }
}
