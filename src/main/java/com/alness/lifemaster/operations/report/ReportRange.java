package com.alness.lifemaster.operations.report;

import java.time.LocalDate;

public record ReportRange(ReportPeriod period, LocalDate from, LocalDate to) {
}
