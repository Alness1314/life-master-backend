package com.alness.lifemaster.validation;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.expenses.dto.request.ExpensesRequest;
import com.alness.lifemaster.debts.dto.request.DebtsRequest;

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

    @Test
    void acceptsDebtAmountsWithUpToEightDecimals() {
        DebtsRequest request = DebtsRequest.builder()
                .creditorName("Banco")
                .totalAmount(new BigDecimal("125.12345678"))
                .currency("MXN")
                .hasInterest(false)
                .numberOfPayments(1)
                .paymentsMade(0)
                .dueDate("2026-08-10")
                .isFullyPaid(false)
                .payments(List.of())
                .disbursesFunds(false)
                .build();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsAmountsWithMoreThanEightDecimals() {
        ExpensesRequest request = ExpensesRequest.builder()
                .bankOrEntity("Cash")
                .description("Precision")
                .amount(new BigDecimal("125.123456789"))
                .category("79ab8aab-7e95-4a6f-80b3-5bb04cb0c096")
                .paymentDate("2026-08-10")
                .paymentStatus(true)
                .build();

        assertThat(validator.validate(request))
                .anyMatch(violation -> violation.getPropertyPath().toString().equals("amount"));
    }
}
