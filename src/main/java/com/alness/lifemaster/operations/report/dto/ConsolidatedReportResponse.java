package com.alness.lifemaster.operations.report.dto;

import java.math.BigDecimal;

import com.alness.lifemaster.operations.report.ReportRange;

public record ConsolidatedReportResponse(
        ReportRange range,
        String currency,
        Financial financial,
        Activity activity) {

    public record Financial(
            BigDecimal income,
            BigDecimal expenses,
            BigDecimal paidExpenses,
            BigDecimal pendingExpenses,
            BigDecimal debtProceeds,
            BigDecimal debtPayments,
            BigDecimal debtPrincipalPaid,
            BigDecimal debtInterestPaid,
            BigDecimal outstandingDebt,
            BigDecimal operatingResult,
            BigDecimal netCashFlow) {
    }

    public record Activity(
            long assistanceRecords,
            long retards,
            long absences,
            long exerciseSessions,
            long exerciseMinutes,
            long nutritionMeals,
            long knownCalories) {
    }
}
