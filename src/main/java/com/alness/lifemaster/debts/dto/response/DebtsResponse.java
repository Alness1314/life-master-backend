package com.alness.lifemaster.debts.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
public class DebtsResponse {
    private UUID id;
    private String creditorName;
    private BigDecimal totalAmount;
    private String currency;
    private Boolean hasInterest;
    private Integer numberOfPayments;
    private Integer paymentsMade;
    private LocalDate dueDate;
    private Boolean isFullyPaid;
    private String notes;
    private List<PaymentResponse> payments;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private Boolean erased;
}
