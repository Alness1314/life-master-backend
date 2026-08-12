package com.alness.lifemaster.operations.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class ReportValueFormatter {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private ReportValueFormatter() {
    }

    static String text(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDate date) return DATE.format(date);
        if (value instanceof LocalDateTime dateTime) return DATE_TIME.format(dateTime);
        if (value instanceof LocalTime time) return TIME.format(time);
        if (value instanceof BigDecimal decimal) return decimal.stripTrailingZeros().toPlainString();
        return String.valueOf(value);
    }
}
