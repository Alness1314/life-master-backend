package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface RecurringMovementRepository extends JpaRepository<RecurringMovementEntity, UUID> {
    List<RecurringMovementEntity> findAllByUserIdAndErasedFalseOrderByNextExecutionDate(UUID userId);
    Optional<RecurringMovementEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RecurringMovementEntity> findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(
            UUID userId, LocalDate through);
}
