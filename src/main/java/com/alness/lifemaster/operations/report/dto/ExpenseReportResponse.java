package com.alness.lifemaster.operations.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record ExpenseReportResponse(
        ReportRange range,
        String currency,
        long records,
        BigDecimal total,
        BigDecimal paid,
        BigDecimal pending,
        Map<String, BigDecimal> totalsByCategory,
        List<Item> items) {

    public record Item(
            UUID id,
            LocalDate paymentDate,
            String description,
            String bankOrEntity,
            BigDecimal amount,
            String currency,
            Boolean paid,
            UUID categoryId,
            String category,
            UUID accountId,
            UUID paymentMethodId) {
    }
}
