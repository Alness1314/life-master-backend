package com.alness.lifemaster.finance.summary;

import java.math.BigDecimal;
import java.util.List;

public record MonthlySummaryResponse(
        int year,
        int month,
        String currency,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal paidExpenses,
        BigDecimal pendingExpenses,
        BigDecimal netBalance,
        BigDecimal operatingBalance,
        BigDecimal debtProceeds,
        BigDecimal debtPayments,
        BigDecimal debtPrincipalPaid,
        BigDecimal debtInterestPaid,
        BigDecimal scheduledDebtPayments,
        BigDecimal freeMargin,
        BigDecimal availableAccountBalance,
        BigDecimal totalDebt,
        BigDecimal outstandingDebt,
        List<CategoryExpenseSummary> expensesByCategory) {
}
