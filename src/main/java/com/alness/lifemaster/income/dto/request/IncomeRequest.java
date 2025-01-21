package com.alness.lifemaster.income.dto.request;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncomeRequest {
    private String source;
    private String description;
    private BigDecimal amount;
    private String paymentDate;
}
