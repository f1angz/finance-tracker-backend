package com.finance.tracker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.entity.Category;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.service.BankStatementParser.RawTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private static final int BATCH_SIZE = 30;

    private final BankStatementParser parser;
    private final DeepSeekClient deepSeekClient;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public record ImportResultDto(int imported, int skipped) {}

    // Дефолтные категории: slug → [русское название, тип]
    private static final Map<String, String[]> CATEGORY_DEFS = Map.ofEntries(
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

    @Transactional
    public ImportResultDto importStatement(UUID userId, MultipartFile file) throws Exception {
        List<RawTransaction> raw = parser.parse(file.getInputStream());
        if (raw.isEmpty()) return new ImportResultDto(0, 0);

        User user = userRepository.getReferenceById(userId);
        ensureDefaultCategories(userId, user);

        List<Category> userCategories = categoryRepository.findByUserIdOrderByNameAsc(userId);
        Set<String> validSlugs = userCategories.stream().map(Category::getSlug).collect(Collectors.toSet());

        List<CategorizedTx> categorized = categorizeWithAi(raw, userCategories, validSlugs);

        int imported = 0, skipped = 0;

        for (CategorizedTx ctx : categorized) {
            if ("skip".equals(ctx.slug())) { skipped++; continue; }

            Category category = getOrCreateCategory(userId, user, ctx.slug(), ctx.type());

            Transaction tx = Transaction.builder()
                    .user(user)
                    .title(ctx.title())
                    .description("")
                    .amount(ctx.raw().amount())
                    .categorySlug(category.getSlug())
                    .date(ctx.raw().date())
                    .time("")
                    .type(Transaction.TransactionType.valueOf(ctx.type()))
                    .build();

            transactionRepository.save(tx);
            imported++;
        }

        return new ImportResultDto(imported, skipped);
    }

    private List<CategorizedTx> categorizeWithAi(List<RawTransaction> transactions,
                                                   List<Category> userCategories,
                                                   Set<String> validSlugs) {
        String systemPrompt = buildSystemPrompt(userCategories);
        List<CategorizedTx> result = new ArrayList<>();

        for (int start = 0; start < transactions.size(); start += BATCH_SIZE) {
            List<RawTransaction> batch = transactions.subList(start, Math.min(start + BATCH_SIZE, transactions.size()));
            result.addAll(categorizeBatch(batch, start, systemPrompt, validSlugs));
        }

        return result;
    }

    private List<CategorizedTx> categorizeBatch(List<RawTransaction> batch, int offset,
                                                  String systemPrompt, Set<String> validSlugs) {
        StringBuilder userMsg = new StringBuilder("Категоризируй транзакции:\n");
        for (int i = 0; i < batch.size(); i++) {
            RawTransaction t = batch.get(i);
            userMsg.append(String.format("%d. %s (%.2f₽, %s)%n",
                    offset + i + 1, t.description(), t.amount(),
                    t.income() ? "доход" : "расход"));
        }

        try {
            String json = deepSeekClient.chatJson(systemPrompt, userMsg.toString());
            log.debug("DeepSeek response for batch [{}-{}]: {}", offset + 1, offset + batch.size(), json);

            List<AiItem> items = objectMapper.readValue(
                    objectMapper.readTree(json).get("items").toString(),
                    new TypeReference<>() {}
            );

            return items.stream()
                    .filter(item -> item.index() >= offset + 1 && item.index() <= offset + batch.size())
                    .map(item -> {
                        RawTransaction raw = batch.get(item.index() - offset - 1);
                        String slug = resolveSlug(item.slug(), item.type(), validSlugs);
                        return new CategorizedTx(raw, slug, item.title(), item.type());
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("DeepSeek categorization failed for batch [{}-{}]: {}", offset + 1, offset + batch.size(), e.getMessage(), e);
            return batch.stream()
                    .map(t -> new CategorizedTx(t,
                            t.income() ? "transfer_in" : "other",
                            t.description(),
                            t.income() ? "INCOME" : "EXPENSE"))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Строит системный промпт с категориями пользователя из БД.
     * Если категорий нет — использует дефолтный набор.
     */
    private String buildSystemPrompt(List<Category> userCategories) {
        String expenseCategories;
        String incomeCategories;

        if (userCategories.isEmpty()) {
            expenseCategories = "food (Продукты), restaurants (Кафе и рестораны), transport (Транспорт), " +
                    "shopping (Покупки), entertainment (Развлечения), education (Образование), " +
                    "utilities (Коммуналка и связь), health (Здоровье), transfers (Переводы), other (Другое)";
            incomeCategories = "salary (Зарплата), transfer_in (Входящий перевод), cashback (Кэшбэк)";
        } else {
            expenseCategories = userCategories.stream()
                    .filter(c -> c.getType() == Category.CategoryType.EXPENSE)
                    .map(c -> c.getSlug() + " (" + c.getName() + ")")
                    .collect(Collectors.joining(", "));
            incomeCategories = userCategories.stream()
                    .filter(c -> c.getType() == Category.CategoryType.INCOME)
                    .map(c -> c.getSlug() + " (" + c.getName() + ")")
                    .collect(Collectors.joining(", "));
            if (expenseCategories.isBlank()) expenseCategories = "other (Другое)";
            if (incomeCategories.isBlank()) incomeCategories = "transfer_in (Входящий перевод)";
        }

        return """
                Ты классификатор банковских транзакций. Используй ТОЛЬКО slug из списка ниже.

                Категории расходов: %s.
                Категории доходов: %s.

                Правила:
                - Используй "skip" для внутренних переводов (Внутренний перевод на договор, Внутрибанковский перевод с договора, Снятие наличных ATM, Внесение наличных).
                - YandexGo, Uber, Яндекс Такси → transport.
                - Магнит, Пятёрочка, ВкусВилл, Лента → food.
                - Кафе, ресторан, суши, пицца → restaurants.
                - Netflix, Spotify, кино, Steam → entertainment.
                - Аптека, поликлиника, стоматолог → health.
                - title — короткое понятное название на русском (например "Яндекс Go", "Магнит", "Зарплата").
                - type — INCOME для доходов, EXPENSE для расходов.

                Верни ТОЛЬКО JSON без пояснений:
                {"items":[{"index":1,"slug":"transport","title":"Яндекс Go","type":"EXPENSE"}]}
                """.formatted(expenseCategories, incomeCategories);
    }

    /** Возвращает slug если он валиден, иначе fallback: transfer_in для доходов, other для расходов */
    private String resolveSlug(String slug, String type, Set<String> validSlugs) {
        if ("skip".equals(slug)) return "skip";
        if (validSlugs.contains(slug)) return slug;
        log.warn("AI returned unknown slug '{}', falling back to default", slug);
        return "INCOME".equals(type) ? "transfer_in" : "other";
    }

    /** Создаёт все дефолтные категории для пользователя если их ещё нет */
    private void ensureDefaultCategories(UUID userId, User user) {
        for (Map.Entry<String, String[]> entry : CATEGORY_DEFS.entrySet()) {
            String slug = entry.getKey();
            if (!categoryRepository.existsByUserIdAndSlug(userId, slug)) {
                String[] def = entry.getValue();
                categoryRepository.save(Category.builder()
                        .user(user)
                        .slug(slug)
                        .name(def[0])
                        .type(Category.CategoryType.valueOf(def[1]))
                        .build());
            }
        }
    }

    private Category getOrCreateCategory(UUID userId, User user, String slug, String type) {
        return categoryRepository.findByUserIdAndSlug(userId, slug)
                .orElseGet(() -> {
                    String[] def = CATEGORY_DEFS.getOrDefault(slug, new String[]{slug, type});
                    Category c = Category.builder()
                            .user(user)
                            .slug(slug)
                            .name(def[0])
                            .type(Category.CategoryType.valueOf(def[1]))
                            .build();
                    return categoryRepository.save(c);
                });
    }

    private record AiItem(int index, String slug, String title, String type) {}
    private record CategorizedTx(RawTransaction raw, String slug, String title, String type) {}
}
