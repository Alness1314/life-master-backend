package com.alness.lifemaster.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;

class FinancialRequestValidationTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsNonPositiveExpenseAmount() {
        ExpensesRequest request = ExpensesRequest.builder()
                .bankOrEntity("Cash")
                .description("Test")
                .amount(BigDecimal.ZERO)
                .category("79ab8aab-7e95-4a6f-80b3-5bb04cb0c096")
                .paymentDate("2026-07-26")
                .paymentStatus(true)
                .build();

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("amount"));
    }

    @Test
    void acceptsValidExpense() {
        ExpensesRequest request = ExpensesRequest.builder()
                .bankOrEntity("Cash")
                .description("Groceries")
                .amount(new BigDecimal("125.50"))
                .category("79ab8aab-7e95-4a6f-80b3-5bb04cb0c096")
                .paymentDate("2026-07-26")
                .paymentStatus(true)
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }
}
