package com.finance.tracker.dto.category;

import com.finance.tracker.entity.Category.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Категория транзакций")
public class CategoryDto {
    private String id;

    @NotBlank
    @Schema(example = "Продукты")
    private String name;

    @NotBlank
    @Schema(example = "products")
    private String slug;

    @Schema(description = "Количество операций (только в ответе)")
    private int operationsCount;

    @Schema(description = "Суммарный оборот (только в ответе)")
    private BigDecimal totalAmount;

    @NotNull
    @Schema(example = "EXPENSE")
    private CategoryType type;
}
