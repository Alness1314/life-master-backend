package com.alness.lifemaster.income.dto.request;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomeRequest {
    @NotBlank
    @Size(max = 128)
    private String source;
    @NotBlank
    @Size(max = 512)
    private String description;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String paymentDate;
    private UUID accountId;
    @Pattern(regexp = "^[A-Z]{3}$")
    private String currency;
}
