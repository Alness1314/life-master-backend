package com.alness.lifemaster.income.repository;

import java.util.UUID;
import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alness.lifemaster.income.entity.IncomeEntity;

public interface IncomeRepository extends JpaRepository<IncomeEntity, UUID>, JpaSpecificationExecutor<IncomeEntity>{
    List<IncomeEntity> findAllByUserIdAndPaymentDateBetweenAndErasedFalse(
            UUID userId, LocalDate from, LocalDate to);
    List<IncomeEntity> findAllByAccountIdAndErasedFalse(UUID accountId);
}
