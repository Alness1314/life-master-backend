package com.alness.lifemaster.operations.bankimport;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BankImportMovementPreview(
        int rowNumber,
        LocalDate date,
        String description,
        BigDecimal amount,
        String type,
        String currency) {

    static BankImportMovementPreview from(BankMovement movement) {
        return new BankImportMovementPreview(movement.rowNumber(), movement.date(), movement.description(), movement.amount(),
                movement.type().name(), movement.currency());
    }
}
