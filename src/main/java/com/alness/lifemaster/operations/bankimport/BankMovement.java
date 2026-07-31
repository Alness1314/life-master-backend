package com.alness.lifemaster.operations.bankimport;

import java.math.BigDecimal;
import java.time.LocalDate;

record BankMovement(int rowNumber, LocalDate date, String description, BigDecimal amount,
        MovementType type, String currency) {
}

enum MovementType {
    EXPENSE,
    INCOME
}
