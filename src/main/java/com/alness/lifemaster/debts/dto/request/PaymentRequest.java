package com.alness.lifemaster.debts.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String paymentDate;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amountPaid;
    @NotBlank
    @Size(max = 128)
    private String paymentMethod;
    @NotNull
    private Boolean isPaid;
    @Size(max = 2000)
    private String notes;
    private UUID paymentMethodId;
}
