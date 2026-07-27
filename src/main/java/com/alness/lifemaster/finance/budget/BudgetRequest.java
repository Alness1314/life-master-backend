package com.alness.lifemaster.finance.budget;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.*;

public record BudgetRequest(
        UUID categoryId,
        @NotNull @Min(2000) @Max(2200) Integer year,
        @NotNull @Min(1) @Max(12) Integer month,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull @Min(1) @Max(100) Integer alertPercentage) {
}
