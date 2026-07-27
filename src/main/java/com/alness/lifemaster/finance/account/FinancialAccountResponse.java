package com.alness.lifemaster.finance.account;

import java.math.BigDecimal;
import java.util.UUID;

public record FinancialAccountResponse(UUID id, String name, AccountType accountType, String currency,
        BigDecimal initialBalance, BigDecimal currentBalance, Boolean active) {
}
