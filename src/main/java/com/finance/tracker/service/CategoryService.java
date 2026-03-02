package com.finance.tracker.service;

import com.finance.tracker.dto.category.CategoryDto;
import com.finance.tracker.entity.Category;
import com.finance.tracker.entity.Category.CategoryType;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.BadRequestException;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<CategoryDto> getCategories(UUID userId, String type) {
        List<Category> categories = (type != null && !type.isBlank())
                ? categoryRepository.findByUserIdAndTypeOrderByNameAsc(userId, CategoryType.valueOf(type))
                : categoryRepository.findByUserIdOrderByNameAsc(userId);

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        return categories.stream().map(c -> toDto(c, userId, monthStart, monthEnd)).toList();
    }

    @Transactional
    public CategoryDto createCategory(UUID userId, CategoryDto dto) {
        if (categoryRepository.existsByUserIdAndSlug(userId, dto.getSlug())) {
            throw new BadRequestException("Категория с таким slug уже существует");
        }
        User user = userRepository.getReferenceById(userId);

        Category category = Category.builder()
                .user(user)
                .name(dto.getName())
                .slug(dto.getSlug())
                .type(dto.getType())
                .build();

        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
        return toDto(categoryRepository.save(category), userId, monthStart, monthEnd);
    }

    @Transactional
    public void deleteCategory(UUID userId, String id) {
        Category category = categoryRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));
        categoryRepository.delete(category);
    }

    private CategoryDto toDto(Category c, UUID userId, LocalDate from, LocalDate to) {
        CategoryDto dto = new CategoryDto();
        dto.setId(c.getId().toString());
        dto.setName(c.getName());
        dto.setSlug(c.getSlug());
        dto.setType(c.getType());

        // Compute stats for current month
        BigDecimal total = transactionRepository.sumExpensesByUserIdAndCategorySlugAndPeriod(userId, c.getSlug(), from, to);
        dto.setTotalAmount(total != null ? total : BigDecimal.ZERO);
        dto.setOperationsCount(0); // simplified — add a count query if needed
        return dto;
    }
}
