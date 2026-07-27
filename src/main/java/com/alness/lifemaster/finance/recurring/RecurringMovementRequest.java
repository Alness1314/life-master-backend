package com.alness.lifemaster.finance.recurring;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.*;

public record RecurringMovementRequest(
        @NotNull MovementType movementType,
        @NotBlank @Size(max = 256) String description,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        UUID categoryId,
        UUID accountId,
        UUID paymentMethodId,
        @NotNull RecurrenceFrequency frequency,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotNull Boolean active) {

    @AssertTrue(message = "Expense recurrences require a category")
    public boolean isCategoryValid() {
        return movementType != MovementType.EXPENSE || categoryId != null;
    }

    @AssertTrue(message = "End date must not be before start date")
    public boolean isDateRangeValid() {
        return endDate == null || startDate == null || !endDate.isBefore(startDate);
    }
}
