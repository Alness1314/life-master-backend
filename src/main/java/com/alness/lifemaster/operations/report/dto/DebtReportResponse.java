package com.alness.lifemaster.operations.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record DebtReportResponse(
        ReportRange range,
        String currency,
        long debts,
        long overdueDebts,
        BigDecimal originalAmount,
        BigDecimal paidPrincipal,
        BigDecimal outstandingAmount,
        BigDecimal receivedInPeriod,
        BigDecimal paidInPeriod,
        BigDecimal principalPaidInPeriod,
        BigDecimal interestPaidInPeriod,
        BigDecimal scheduledInPeriod,
        List<Item> items) {

    public record Item(
            UUID id,
            String creditor,
            BigDecimal totalAmount,
            BigDecimal paidPrincipal,
            BigDecimal outstandingAmount,
            BigDecimal progressPercentage,
            LocalDate dueDate,
            Boolean fullyPaid,
            Boolean overdue,
            Boolean disbursesFunds,
            BigDecimal receivedAmount,
            LocalDate receivedDate,
            List<PaymentItem> periodPayments) {
    }

    public record PaymentItem(
            UUID id,
            LocalDate paymentDate,
            BigDecimal amount,
            BigDecimal principal,
            BigDecimal interest,
            Boolean paid,
            String paymentMethod,
            UUID accountId) {
    }
}
