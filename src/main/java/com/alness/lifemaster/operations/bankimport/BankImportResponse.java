package com.alness.lifemaster.operations.bankimport;

import java.util.List;

public record BankImportResponse(boolean dryRun, int totalRows, int expenses, int income, List<String> warnings) {
}
