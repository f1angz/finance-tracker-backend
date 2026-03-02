package com.finance.tracker.service;

import com.finance.tracker.dto.goal.CreateGoalRequest;
import com.finance.tracker.dto.goal.GoalContributionRequest;
import com.finance.tracker.dto.goal.GoalDto;
import com.finance.tracker.entity.Goal;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.repository.GoalRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public List<GoalDto> getGoals(UUID userId) {
        return goalRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public GoalDto createGoal(UUID userId, CreateGoalRequest request) {
        User user = userRepository.getReferenceById(userId);

        Goal goal = Goal.builder()
                .user(user)
                .emoji(request.getEmoji())
                .title(request.getTitle())
                .targetAmount(request.getTargetAmount())
                .targetDate(LocalDate.parse(request.getTargetDate()))
                .accentColor(request.getAccentColor() != null ? request.getAccentColor() : "3B82F6")
                .build();

        return toDto(goalRepository.save(goal));
    }

    @Transactional
    public GoalDto addContribution(UUID userId, String id, GoalContributionRequest request) {
        Goal goal = goalRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Цель не найдена"));

        goal.setSavedAmount(goal.getSavedAmount().add(request.getAmount()));
        return toDto(goalRepository.save(goal));
    }

    @Transactional
    public void deleteGoal(UUID userId, String id) {
        Goal goal = goalRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Цель не найдена"));
        goalRepository.delete(goal);
    }

    private GoalDto toDto(Goal g) {
        GoalDto dto = new GoalDto();
        dto.setId(g.getId().toString());
        dto.setEmoji(g.getEmoji());
        dto.setTitle(g.getTitle());
        dto.setSavedAmount(g.getSavedAmount());
        dto.setTargetAmount(g.getTargetAmount());
        dto.setAccentColor(g.getAccentColor());
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), g.getTargetDate());
        dto.setDaysLeft((int) Math.max(0, daysLeft));
        return dto;
    }
}
