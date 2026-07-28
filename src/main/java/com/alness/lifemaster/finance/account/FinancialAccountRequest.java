package com.alness.lifemaster.finance.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.*;
import com.alness.lifemaster.common.currency.ValidCurrency;

public record FinancialAccountRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull AccountType accountType,
        @NotBlank @ValidCurrency String currency,
        @NotNull @Digits(integer = 13, fraction = 8) BigDecimal initialBalance,
        @NotNull Boolean active) {
}
