package com.alness.lifemaster.operations.report.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.alness.lifemaster.operations.report.ReportRange;

public record AssistanceReportResponse(
        ReportRange range,
        long records,
        long onTime,
        long retards,
        long justifiedAbsences,
        long unjustifiedAbsences,
        long workedMinutes,
        List<Item> items) {

    public record Item(
            UUID id,
            LocalDate workDate,
            LocalTime timeEntry,
            LocalTime departureTime,
            Boolean onTime,
            Boolean retard,
            Boolean justifiedAbsence,
            Boolean unjustifiedAbsence,
            long workedMinutes) {
    }
}
