package com.finance.tracker.service;

import com.finance.tracker.dto.limit.LimitDto;
import com.finance.tracker.dto.limit.LimitRequest;
import com.finance.tracker.entity.Limit;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.repository.LimitRepository;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LimitService {

    private final LimitRepository limitRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter PERIOD_FMT = DateTimeFormatter.ofPattern("YYYY-MM");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("ru"));

    public List<LimitDto> getLimits(UUID userId, String period) {
        String p = (period != null && !period.isBlank()) ? period : YearMonth.now().format(PERIOD_FMT);
        return limitRepository.findByUserIdAndPeriodOrderByCategoryNameAsc(userId, p)
                .stream().map(l -> toDto(l, userId)).toList();
    }

    @Transactional
    public LimitDto createLimit(UUID userId, LimitRequest request) {
        User user = userRepository.getReferenceById(userId);

        Limit limit = Limit.builder()
                .user(user)
                .categorySlug(request.getCategorySlug())
                .categoryName(request.getCategorySlug()) // will be updated from category name
                .period(request.getPeriod())
                .limitAmount(request.getAmount())
                .build();

        return toDto(limitRepository.save(limit), userId);
    }

    @Transactional
    public LimitDto updateLimit(UUID userId, String id, LimitRequest request) {
        Limit limit = limitRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Лимит не найден"));

        limit.setCategorySlug(request.getCategorySlug());
        limit.setPeriod(request.getPeriod());
        limit.setLimitAmount(request.getAmount());

        return toDto(limitRepository.save(limit), userId);
    }

    @Transactional
    public void deleteLimit(UUID userId, String id) {
        Limit limit = limitRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Лимит не найден"));
        limitRepository.delete(limit);
    }

    private LimitDto toDto(Limit l, UUID userId) {
        YearMonth ym = YearMonth.parse(l.getPeriod());
        LocalDate from = ym.atDay(1);
        LocalDate to = ym.atEndOfMonth();

        BigDecimal spent = transactionRepository.sumExpensesByUserIdAndCategorySlugAndPeriod(
                userId, l.getCategorySlug(), from, to);

        LimitDto dto = new LimitDto();
        dto.setId(l.getId().toString());
        dto.setCategorySlug(l.getCategorySlug());
        dto.setCategoryName(l.getCategoryName());
        dto.setPeriod(ym.format(DISPLAY_FMT));
        dto.setLimitAmount(l.getLimitAmount());
        dto.setSpentAmount(spent != null ? spent : BigDecimal.ZERO);
        return dto;
    }
}
