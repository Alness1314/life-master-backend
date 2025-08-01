package com.alness.lifemaster.debts.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private String paymentDate;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private Boolean isPaid;
    private String notes;
}
