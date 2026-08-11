package com.alness.lifemaster.debts.dto.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import com.alness.lifemaster.common.currency.ValidCurrency;

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
    @Digits(integer = 13, fraction = 8)
    private BigDecimal totalAmount;
    @NotBlank
    @ValidCurrency
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
    @NotNull
    private Boolean disbursesFunds;
    @DecimalMin("0.01")
    @Digits(integer = 13, fraction = 8)
    private BigDecimal receivedAmount;
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String receivedDate;
    private UUID depositAccountId;

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
                .map(payment -> payment.getPrincipalAmount() == null
                        ? payment.getAmountPaid()
                        : payment.getPrincipalAmount())
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return paid.compareTo(totalAmount) <= 0;
    }

    @AssertTrue(message = "Los datos del desembolso son obligatorios cuando la deuda entrega dinero")
    public boolean isDisbursementValid() {
        if (!Boolean.TRUE.equals(disbursesFunds)) {
            return receivedAmount == null && receivedDate == null && depositAccountId == null;
        }
        return receivedAmount != null && receivedAmount.signum() > 0
                && receivedDate != null && depositAccountId != null;
    }
}
