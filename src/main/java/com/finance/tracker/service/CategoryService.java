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
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final Map<String, String[]> DEFAULT_CATEGORY_DEFS = Map.ofEntries(
            Map.entry("food",          new String[]{"Продукты", "EXPENSE"}),
            Map.entry("restaurants",   new String[]{"Кафе и рестораны", "EXPENSE"}),
            Map.entry("transport",     new String[]{"Транспорт", "EXPENSE"}),
            Map.entry("shopping",      new String[]{"Покупки", "EXPENSE"}),
            Map.entry("entertainment", new String[]{"Развлечения", "EXPENSE"}),
            Map.entry("education",     new String[]{"Образование", "EXPENSE"}),
            Map.entry("utilities",     new String[]{"Коммуналка и связь", "EXPENSE"}),
            Map.entry("health",        new String[]{"Здоровье", "EXPENSE"}),
            Map.entry("transfers",     new String[]{"Переводы", "EXPENSE"}),
            Map.entry("other",         new String[]{"Другое", "EXPENSE"}),
            Map.entry("salary",        new String[]{"Зарплата", "INCOME"}),
            Map.entry("transfer_in",   new String[]{"Входящий перевод", "INCOME"}),
            Map.entry("cashback",      new String[]{"Кэшбэк", "INCOME"})
    );

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createDefaultCategories(User user) {
        DEFAULT_CATEGORY_DEFS.forEach((slug, def) ->
                categoryRepository.save(Category.builder()
                        .user(user)
                        .slug(slug)
                        .name(def[0])
                        .type(CategoryType.valueOf(def[1]))
                        .build())
        );
    }

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
    public CategoryDto updateCategory(UUID userId, String id, CategoryDto dto) {
        Category category = categoryRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Категория не найдена"));

        if (!category.getSlug().equals(dto.getSlug()) && categoryRepository.existsByUserIdAndSlug(userId, dto.getSlug())) {
            throw new BadRequestException("Категория с таким slug уже существует");
        }

        category.setName(dto.getName());
        category.setSlug(dto.getSlug());
        category.setType(dto.getType());

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

        BigDecimal total = transactionRepository.sumExpensesByUserIdAndCategorySlugAndPeriod(userId, c.getSlug(), from, to);
        dto.setTotalAmount(total != null ? total : BigDecimal.ZERO);
        dto.setOperationsCount(0);
        return dto;
    }
}
