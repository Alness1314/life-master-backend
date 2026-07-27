package com.alness.lifemaster.expenses.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
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
public class ExpensesRequest {
     @NotNull
    @NotEmpty
    @Size(max = 128)
    private String bankOrEntity;

    @NotNull
    @NotEmpty
    @Size(max = 512)
    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    @Pattern(regexp = "^[0-9a-fA-F-]{36}$")
    private String category;
    
    @NotNull
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    private String paymentDate;
    
    @NotNull
    private Boolean paymentStatus;
}
