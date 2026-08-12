package com.alness.lifemaster.operations.report;

import java.util.List;
import java.util.Map;

public record ReportDocument(
        String title,
        ReportRange range,
        String currency,
        Map<String, Object> summary,
        List<String> columns,
        List<List<Object>> rows) {
}
