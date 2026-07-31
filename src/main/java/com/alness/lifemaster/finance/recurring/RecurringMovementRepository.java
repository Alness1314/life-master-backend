package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface RecurringMovementRepository extends JpaRepository<RecurringMovementEntity, UUID> {
    List<RecurringMovementEntity> findAllByUserIdAndErasedFalseOrderByNextExecutionDate(UUID userId);
    Optional<RecurringMovementEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RecurringMovementEntity> findAllByUserIdAndActiveTrueAndErasedFalseAndNextExecutionDateLessThanEqual(
            UUID userId, LocalDate through);

    @Query("""
            select recurring.id
            from RecurringMovementEntity recurring
            where recurring.active = true
              and recurring.erased = false
              and recurring.nextExecutionDate <= :through
            order by recurring.nextExecutionDate, recurring.id
            """)
    List<UUID> findDueIds(@Param("through") LocalDate through);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select recurring
            from RecurringMovementEntity recurring
            where recurring.id = :id
              and recurring.active = true
              and recurring.erased = false
            """)
    Optional<RecurringMovementEntity> findActiveByIdForUpdate(@Param("id") UUID id);
}
