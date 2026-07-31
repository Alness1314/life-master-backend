package com.alness.lifemaster.operations.bankimport;

import java.util.List;

record ParsedBankStatement(
        String format,
        List<BankMovement> movements,
        List<BankImportFailurePreview> failures,
        List<String> warnings) {
}
