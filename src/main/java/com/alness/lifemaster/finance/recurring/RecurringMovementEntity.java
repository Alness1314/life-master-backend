package com.alness.lifemaster.finance.recurring;

import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;

import com.alness.lifemaster.categories.entity.CategoryEntity;
import com.alness.lifemaster.finance.account.FinancialAccountEntity;
import com.alness.lifemaster.finance.paymentmethod.PaymentMethodEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "recurring_movements")
@Getter
@Setter
public class RecurringMovementEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private FinancialAccountEntity account;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethodEntity paymentMethod;
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 16)
    private MovementType movementType;
    @Column(nullable = false, length = 256)
    private String description;
    @Column(nullable = false, precision = 21, scale = 8)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RecurrenceFrequency frequency;
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "end_date")
    private LocalDate endDate;
    @Column(name = "next_execution_date", nullable = false)
    private LocalDate nextExecutionDate;
    @Column(nullable = false)
    private Boolean active;
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
