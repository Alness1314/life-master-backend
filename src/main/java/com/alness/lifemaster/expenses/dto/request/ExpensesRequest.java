package com.alness.lifemaster.expenses.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    private String bankOrEntity;

    @NotNull
    @NotEmpty
    private String description;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private String category;
    
    @NotNull
    private String paymentDate;
    
    @NotNull
    private Boolean paymentStatus;
}
