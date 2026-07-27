package com.alness.lifemaster.debts.dto.request;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

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
    @NotBlank
    @Size(max = 256)
    private String creditorName;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalAmount;
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;
    @NotNull
    private Boolean hasInterest;
    @NotNull
    @Min(1)
    private Integer numberOfPayments;
    @NotNull
    @Min(0)
    private Integer paymentsMade;
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String dueDate;
    @NotNull
    private Boolean isFullyPaid;
    @Size(max = 2000)
    private String notes;
    @NotNull
    @Valid
    private List<PaymentRequest> payments;

    @AssertTrue(message = "paymentsMade must not exceed numberOfPayments")
    public boolean isPaymentCountValid() {
        return paymentsMade == null || numberOfPayments == null || paymentsMade <= numberOfPayments;
    }

    @AssertTrue(message = "Paid amounts must not exceed totalAmount")
    public boolean isPaidAmountValid() {
        if (payments == null || totalAmount == null) {
            return true;
        }
        BigDecimal paid = payments.stream()
                .filter(payment -> Boolean.TRUE.equals(payment.getIsPaid()))
                .map(PaymentRequest::getAmountPaid)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return paid.compareTo(totalAmount) <= 0;
    }
}
