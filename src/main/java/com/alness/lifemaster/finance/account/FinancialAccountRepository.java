package com.alness.lifemaster.finance.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccountEntity, UUID> {
    List<FinancialAccountEntity> findAllByUserIdAndErasedFalseOrderByName(UUID userId);
    Optional<FinancialAccountEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    Optional<FinancialAccountEntity> findByUserIdAndNameIgnoreCaseAndErasedFalse(UUID userId, String name);
}
