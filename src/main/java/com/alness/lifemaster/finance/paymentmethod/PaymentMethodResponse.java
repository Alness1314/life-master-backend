package com.alness.lifemaster.finance.paymentmethod;

import java.util.UUID;

public record PaymentMethodResponse(UUID id, String name, PaymentMethodType methodType, UUID accountId,
        Boolean active) {
}
