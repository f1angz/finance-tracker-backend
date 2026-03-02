package com.finance.tracker.service;

import com.finance.tracker.dto.ai.*;
import com.finance.tracker.dto.finance.FinanceStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * AI-ассистент: пока возвращает аналитику на основе реальных данных пользователя.
 * В будущем можно подключить LLM (OpenAI / Anthropic).
 */
@Service
@RequiredArgsConstructor
public class AiService {

    private final FinanceService financeService;

    public AiHealthScoreDto getHealthScore(UUID userId) {
        FinanceStatsDto stats = financeService.getStats(userId);

        int savingsScore = computeSavingsScore(stats.getIncome(), stats.getSavings());
        int expenseScore = computeExpenseScore(stats.getIncome(), stats.getExpense());
        int overall = (savingsScore + expenseScore + 70) / 3;

        return new AiHealthScoreDto(
                Math.min(overall, 100),
                expenseScore,
                savingsScore,
                70,
                75
        );
    }

    public List<AiInsightDto> getInsights(UUID userId) {
        FinanceStatsDto stats = financeService.getStats(userId);
        List<AiInsightDto> insights = new java.util.ArrayList<>();

        if (stats.getExpense().compareTo(stats.getIncome()) >= 0) {
            insights.add(new AiInsightDto("1", "DANGER",
                    "Расходы превышают доходы",
                    "В этом месяце вы тратите больше, чем зарабатываете.",
                    "Пересмотрите бюджет и сократите необязательные расходы."));
        } else {
            insights.add(new AiInsightDto("1", "SUCCESS",
                    "Положительный баланс",
                    "Отлично! Ваши доходы превышают расходы в этом месяце.",
                    "Направьте свободные средства на накопительные цели."));
        }

        if (stats.getIncomeChange() > 10) {
            insights.add(new AiInsightDto("2", "INFO",
                    "Рост доходов",
                    String.format("Доход вырос на %.1f%% по сравнению с прошлым месяцем.", stats.getIncomeChange()),
                    "Хорошая динамика! Рассмотрите инвестирование части прибыли."));
        }

        return insights;
    }

    public List<AiTipDto> getTips(UUID userId) {
        return List.of(
                new AiTipDto("1", "Оптимизация", "Готовьте еду дома вместо кафе", "~3 000₽/мес"),
                new AiTipDto("2", "Доход", "Монетизируйте навыки на фрилансе", "+15 000₽/мес"),
                new AiTipDto("3", "Цели", "Автоматический перевод 10% от дохода в накопления", "По плану")
        );
    }

    public ChatResponse chat(UUID userId, ChatRequest request) {
        FinanceStatsDto stats = financeService.getStats(userId);
        String reply = String.format(
                "Привет! Ваш текущий баланс: %.2f ₽. Доходы: %.2f ₽, Расходы: %.2f ₽. " +
                "Чем могу помочь в управлении финансами?",
                stats.getBalance(), stats.getIncome(), stats.getExpense()
        );
        return new ChatResponse(reply);
    }

    private int computeSavingsScore(BigDecimal income, BigDecimal savings) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 50;
        double ratio = savings.doubleValue() / income.doubleValue();
        return (int) Math.min(100, ratio * 200);
    }

    private int computeExpenseScore(BigDecimal income, BigDecimal expense) {
        if (income.compareTo(BigDecimal.ZERO) == 0) return 50;
        double ratio = expense.doubleValue() / income.doubleValue();
        if (ratio <= 0.5) return 100;
        if (ratio <= 0.7) return 80;
        if (ratio <= 0.9) return 60;
        return 30;
    }
}
