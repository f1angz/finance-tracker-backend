package com.finance.tracker.repository;

import com.finance.tracker.entity.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LimitRepository extends JpaRepository<Limit, UUID> {
    List<Limit> findByUserIdAndPeriodOrderByCategoryNameAsc(UUID userId, String period);
    Optional<Limit> findByIdAndUserId(UUID id, UUID userId);
}
