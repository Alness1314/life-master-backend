package com.alness.lifemaster.debts.dto.request;

import java.math.BigDecimal;
import java.util.List;

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
public class DebtsRequest {
    private String creditorName;
    private BigDecimal totalAmount;
    private String currency;
    private Boolean hasInterest;
    private Integer numberOfPayments;
    private Integer paymentsMade;
    private String dueDate;
    private Boolean isFullyPaid;
    private String notes;
    private List<PaymentRequest> payments;
}
