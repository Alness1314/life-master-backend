package com.alness.lifemaster.operations.receipt;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseReceiptRepository extends JpaRepository<ExpenseReceiptEntity, UUID> {
    List<ExpenseReceiptEntity> findAllByExpenseIdAndUserIdOrderByCreatedAtDesc(UUID expenseId, UUID userId);
    Optional<ExpenseReceiptEntity> findByIdAndUserId(UUID id, UUID userId);
}
