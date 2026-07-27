package com.alness.lifemaster.operations.bankimport;

import java.time.LocalDateTime;
import java.util.UUID;

import com.alness.lifemaster.users.entity.UserEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_imports")
@Getter
@Setter
public class BankImportEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "file_name", nullable = false, length = 256)
    private String fileName;
    @Column(name = "file_hash", nullable = false, length = 64)
    private String fileHash;
    @Column(name = "imported_rows", nullable = false)
    private Integer importedRows;
    @Column(name = "imported_expenses", nullable = false)
    private Integer importedExpenses;
    @Column(name = "imported_income", nullable = false)
    private Integer importedIncome;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        createdAt = LocalDateTime.now();
    }
}
