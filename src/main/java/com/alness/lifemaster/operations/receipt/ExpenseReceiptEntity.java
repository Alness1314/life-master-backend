package com.alness.lifemaster.operations.receipt;

import java.time.LocalDateTime;
import java.util.UUID;

import com.alness.lifemaster.expenses.entity.ExpensesEntity;
import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "expense_receipts")
@Getter
@Setter
public class ExpenseReceiptEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpensesEntity expense;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "original_name", nullable = false, length = 256)
    private String originalName;
    @Column(name = "content_type", nullable = false, length = 128)
    private String contentType;
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] content;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        createdAt = LocalDateTime.now();
    }
}
