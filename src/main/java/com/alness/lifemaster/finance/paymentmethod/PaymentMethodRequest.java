package com.alness.lifemaster.finance.paymentmethod;

import java.util.UUID;

import jakarta.validation.constraints.*;

public record PaymentMethodRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull PaymentMethodType methodType,
        UUID accountId,
        @NotNull Boolean active) {
}
