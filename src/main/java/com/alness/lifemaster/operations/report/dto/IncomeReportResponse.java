package com.alness.lifemaster.operations.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record IncomeReportResponse(
        ReportRange range,
        String currency,
        long records,
        BigDecimal total,
        Map<String, BigDecimal> totalsBySource,
        List<Item> items) {

    public record Item(
            UUID id,
            LocalDate paymentDate,
            String source,
            String description,
            BigDecimal amount,
            String currency,
            UUID accountId) {
    }
}
