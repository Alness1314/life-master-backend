package com.alness.lifemaster.operations.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record ExerciseReportResponse(
        ReportRange range,
        long sessions,
        long totalDurationMinutes,
        BigDecimal averageDurationMinutes,
        Map<String, Long> sessionsByActivity,
        List<Item> items) {

    public record Item(
            UUID id,
            LocalDate trainingDate,
            LocalTime startTime,
            LocalTime endTime,
            String activityType,
            Integer durationMinutes,
            String notes) {
    }
}
