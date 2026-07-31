package com.alness.lifemaster.operations.bankimport;

import java.util.List;

public record BankImportResponse(
        boolean dryRun,
        String format,
        int totalRows,
        int successfulRows,
        int failedRows,
        int expenses,
        int income,
        List<String> warnings,
        List<BankImportMovementPreview> movements,
        List<BankImportFailurePreview> failures) {
}
