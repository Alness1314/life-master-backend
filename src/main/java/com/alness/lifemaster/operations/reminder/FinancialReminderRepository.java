package com.alness.lifemaster.operations.reminder;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

public interface FinancialReminderRepository extends JpaRepository<FinancialReminderEntity, UUID> {
    List<FinancialReminderEntity> findAllByUserIdOrderByScheduledAtDesc(UUID userId);
    Optional<FinancialReminderEntity> findByIdAndUserId(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<FinancialReminderEntity> findAllByDeliveredFalseAndCancelledFalseAndScheduledAtLessThanEqual(LocalDateTime now);
}
