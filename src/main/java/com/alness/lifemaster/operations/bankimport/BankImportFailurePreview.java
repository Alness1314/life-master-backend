package com.alness.lifemaster.operations.bankimport;

public record BankImportFailurePreview(
        int rowNumber,
        String description,
        String reason) {
}
