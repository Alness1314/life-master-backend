package com.alness.lifemaster.finance.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringMovementResponse(UUID id, MovementType movementType, String description, BigDecimal amount,
        String currency, UUID categoryId, UUID accountId, UUID paymentMethodId, RecurrenceFrequency frequency,
        LocalDate startDate, LocalDate endDate, LocalDate nextExecutionDate, Boolean active) {
}
