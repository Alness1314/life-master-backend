package com.alness.lifemaster.finance.paymentmethod;

import java.time.LocalDateTime;
import java.util.UUID;

import com.alness.lifemaster.finance.account.FinancialAccountEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment_methods")
@Getter
@Setter
public class PaymentMethodEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private FinancialAccountEntity account;
    @Column(nullable = false, length = 128)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "method_type", nullable = false, length = 32)
    private PaymentMethodType methodType;
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
