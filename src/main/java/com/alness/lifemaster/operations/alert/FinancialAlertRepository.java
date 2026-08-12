package com.alness.lifemaster.operations.alert;

import java.util.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FinancialAlertRepository extends JpaRepository<FinancialAlertEntity, UUID> {
    List<FinancialAlertEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<FinancialAlertEntity> findByIdAndUserId(UUID id, UUID userId);
    Optional<FinancialAlertEntity> findByUserIdAndReferenceKey(UUID userId, String referenceKey);

    @Modifying
    @Query("update FinancialAlertEntity alert set alert.read = true where alert.user.id = :userId and alert.read = false")
    int markAllReadByUserId(@Param("userId") UUID userId);
}
