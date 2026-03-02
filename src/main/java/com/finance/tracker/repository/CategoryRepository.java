package com.finance.tracker.repository;

import com.finance.tracker.entity.Category;
import com.finance.tracker.entity.Category.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByUserIdOrderByNameAsc(UUID userId);
    List<Category> findByUserIdAndTypeOrderByNameAsc(UUID userId, CategoryType type);
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByUserIdAndSlug(UUID userId, String slug);
}
