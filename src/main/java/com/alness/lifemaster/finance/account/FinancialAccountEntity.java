package com.alness.lifemaster.finance.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "financial_accounts")
@Getter
@Setter
public class FinancialAccountEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(nullable = false, length = 128)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 32)
    private AccountType accountType;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(name = "initial_balance", nullable = false, precision = 21, scale = 8)
    private BigDecimal initialBalance;
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
        createAt = LocalDateTime.now(ZoneId.systemDefault());
        updateAt = createAt;
        erased = false;
    }

    @PreUpdate
    void updateTimestamp() {
        updateAt = LocalDateTime.now(ZoneId.systemDefault());
    }
}
