package com.finance.tracker.service;

import com.finance.tracker.dto.debt.CreateDebtRequest;
import com.finance.tracker.dto.debt.DebtDto;
import com.finance.tracker.entity.Debt;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.repository.DebtRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final UserRepository userRepository;

    public List<DebtDto> getDebts(UUID userId) {
        return debtRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).toList();
    }

    @Transactional
    public DebtDto createDebt(UUID userId, CreateDebtRequest request) {
        User user = userRepository.getReferenceById(userId);

        Debt debt = Debt.builder()
                .user(user)
                .personName(request.getPersonName())
                .type(request.getType())
                .amount(request.getAmount())
                .dueDate(LocalDate.parse(request.getDueDate()))
                .build();

        return toDto(debtRepository.save(debt));
    }

    @Transactional
    public DebtDto markAsPaid(UUID userId, String id) {
        Debt debt = debtRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Долг не найден"));
        debt.setIsPaid(true);
        return toDto(debtRepository.save(debt));
    }

    @Transactional
    public void deleteDebt(UUID userId, String id) {
        Debt debt = debtRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Долг не найден"));
        debtRepository.delete(debt);
    }

    private DebtDto toDto(Debt d) {
        DebtDto dto = new DebtDto();
        dto.setId(d.getId().toString());
        dto.setPersonName(d.getPersonName());
        dto.setType(d.getType());
        dto.setAmount(d.getAmount());
        dto.setDueDate(d.getDueDate().toString());
        dto.setIsPaid(d.getIsPaid());
        return dto;
    }
}
