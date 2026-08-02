package com.alness.lifemaster.finance.account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialAccountRepository extends JpaRepository<FinancialAccountEntity, UUID>,
        JpaSpecificationExecutor<FinancialAccountEntity> {
    List<FinancialAccountEntity> findAllByUserIdAndErasedFalseOrderByName(UUID userId);
    Optional<FinancialAccountEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    Optional<FinancialAccountEntity> findByUserIdAndNameIgnoreCaseAndErasedFalse(UUID userId, String name);
}
