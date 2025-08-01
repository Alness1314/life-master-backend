package com.alness.lifemaster.debts.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

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
public class PaymentResponse {
    private UUID id;
    private LocalDate paymentDate;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private Boolean isPaid;
    private String notes;
}
