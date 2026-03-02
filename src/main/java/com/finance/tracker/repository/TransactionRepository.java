package com.finance.tracker.repository;

import com.finance.tracker.entity.Transaction;
import com.finance.tracker.entity.Transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
          AND (:type IS NULL OR t.type = :type)
          AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY t.date DESC, t.createdAt DESC
        """)
    Page<Transaction> findByUserIdFiltered(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = :type AND t.date BETWEEN :from AND :to")
    BigDecimal sumByUserIdAndTypeAndDateBetween(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.categorySlug = :slug AND t.date BETWEEN :from AND :to")
    BigDecimal sumExpensesByUserIdAndCategorySlugAndPeriod(
            @Param("userId") UUID userId,
            @Param("slug") String slug,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT t.categorySlug, SUM(t.amount)
        FROM Transaction t
        WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.date BETWEEN :from AND :to
        GROUP BY t.categorySlug
        """)
    List<Object[]> sumExpensesGroupedByCategory(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    Optional<Transaction> findByIdAndUserId(UUID id, UUID userId);
}
