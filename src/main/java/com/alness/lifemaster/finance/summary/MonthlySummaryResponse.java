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
        BigDecimal budgetedAmount,
        BigDecimal budgetUsagePercentage,
        BigDecimal totalDebt,
        BigDecimal outstandingDebt,
        List<CategoryExpenseSummary> expensesByCategory) {
}
