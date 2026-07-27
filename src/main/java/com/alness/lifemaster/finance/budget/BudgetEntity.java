package com.alness.lifemaster.finance.budget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "budgets")
@Getter
@Setter
public class BudgetEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    @Column(name = "budget_year", nullable = false)
    private Integer year;
    @Column(name = "budget_month", nullable = false)
    private Integer month;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal amount;
    @Column(name = "alert_percentage", nullable = false)
    private Integer alertPercentage;
    @Column(nullable = false)
    private Boolean erased;
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;
    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @PrePersist
    void create() {
        createAt = LocalDateTime.now();
        updateAt = createAt;
        erased = false;
    }

    @PreUpdate
    void updateTimestamp() {
        updateAt = LocalDateTime.now();
    }
}
