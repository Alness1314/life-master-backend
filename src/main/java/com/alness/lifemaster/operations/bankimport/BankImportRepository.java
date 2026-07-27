package com.alness.lifemaster.operations.bankimport;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BankImportRepository extends JpaRepository<BankImportEntity, UUID> {
    boolean existsByUserIdAndFileHash(UUID userId, String fileHash);
}
