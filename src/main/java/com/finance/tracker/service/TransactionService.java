package com.finance.tracker.service;

import com.finance.tracker.dto.transaction.TransactionDto;
import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.Transaction.TransactionType;
import com.finance.tracker.entity.User;
import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.repository.TransactionRepository;
import com.finance.tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public List<TransactionDto> getTransactions(UUID userId, String type, String search, int page, int limit) {
        TransactionType typeEnum = (type != null && !type.isBlank()) ? TransactionType.valueOf(type) : null;
        String searchParam = (search != null && !search.isBlank()) ? search : "";
        Pageable pageable = PageRequest.of(page - 1, limit);

        var page_ = typeEnum != null
                ? transactionRepository.findByUserIdAndTypeFiltered(userId, typeEnum, searchParam, pageable)
                : transactionRepository.findByUserIdFiltered(userId, searchParam, pageable);

        return page_.stream().map(this::toDto).toList();
    }

    @Transactional
    public TransactionDto createTransaction(UUID userId, TransactionDto dto) {
        User user = userRepository.getReferenceById(userId);

        Transaction tx = Transaction.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription() != null ? dto.getDescription() : "")
                .amount(dto.getAmount())
                .categorySlug(dto.getCategory())
                .date(LocalDate.parse(dto.getDate()))
                .time(dto.getTime() != null ? dto.getTime() : "")
                .type(dto.getType())
                .build();

        return toDto(transactionRepository.save(tx));
    }

    @Transactional
    public void deleteTransaction(UUID userId, String id) {
        Transaction tx = transactionRepository.findByIdAndUserId(UUID.fromString(id), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Транзакция не найдена"));
        transactionRepository.delete(tx);
    }

    public TransactionDto toDto(Transaction tx) {
        TransactionDto dto = new TransactionDto();
        dto.setId(tx.getId().toString());
        dto.setTitle(tx.getTitle());
        dto.setDescription(tx.getDescription());
        dto.setAmount(tx.getAmount());
        dto.setCategory(tx.getCategorySlug());
        dto.setDate(tx.getDate().toString());
        dto.setTime(tx.getTime());
        dto.setType(tx.getType());
        return dto;
    }
}
