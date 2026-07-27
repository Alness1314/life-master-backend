package com.alness.lifemaster.expenses.repository;

import java.util.UUID;
import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.expenses.entity.ExpensesEntity;

public interface ExpensesRepository extends JpaRepository<ExpensesEntity, UUID>, JpaSpecificationExecutor<ExpensesEntity>{
    List<ExpensesEntity> findAllByUserIdAndPaymentDateBetweenAndErasedFalse(
            UUID userId, LocalDate from, LocalDate to);
    List<ExpensesEntity> findAllByAccountIdAndErasedFalse(UUID accountId);
    Optional<ExpensesEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
}
