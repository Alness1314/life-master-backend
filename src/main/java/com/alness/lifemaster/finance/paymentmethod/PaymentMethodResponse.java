package com.alness.lifemaster.finance.paymentmethod;

import java.util.UUID;

import com.alness.lifemaster.finance.account.FinancialAccountResponse;

public record PaymentMethodResponse(UUID id, String name, PaymentMethodType methodType, UUID accountId,
        FinancialAccountResponse account, Boolean active) {
}
