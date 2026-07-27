package com.alness.lifemaster.finance.budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    List<BudgetEntity> findAllByUserIdAndYearAndMonthAndErasedFalse(
            UUID userId, Integer year, Integer month);
    Optional<BudgetEntity> findByIdAndUserIdAndErasedFalse(UUID id, UUID userId);
    boolean existsByUserIdAndYearAndMonthAndCurrencyAndCategoryIdAndErasedFalse(
            UUID userId, Integer year, Integer month, String currency, UUID categoryId);
    boolean existsByUserIdAndYearAndMonthAndCurrencyAndCategoryIsNullAndErasedFalse(
            UUID userId, Integer year, Integer month, String currency);
}
