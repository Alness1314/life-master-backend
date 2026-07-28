package com.alness.lifemaster.operations.alert;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAlertRepository extends JpaRepository<FinancialAlertEntity, UUID> {
    List<FinancialAlertEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<FinancialAlertEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<FinancialAlertEntity> findByUserIdAndReferenceKey(UUID userId, String referenceKey);
}
