package com.alness.lifemaster.operations.receipt;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseReceiptResponse(UUID id, UUID expenseId, String originalName, String contentType,
        Long sizeBytes, LocalDateTime createdAt) {
}
