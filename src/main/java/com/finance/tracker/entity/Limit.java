package com.finance.tracker.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "limits",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_slug", "period"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Limit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category_slug", nullable = false, length = 100)
    private String categorySlug;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    /** Format: YYYY-MM */
    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "limit_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
