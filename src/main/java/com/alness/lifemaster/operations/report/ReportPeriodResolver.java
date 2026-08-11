package com.alness.lifemaster.operations.report;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.exceptions.RestExceptionHandler;
import com.alness.lifemaster.utils.ApiCodes;

@Component
public class ReportPeriodResolver {

    public ReportRange resolve(ReportPeriod period, LocalDate referenceDate) {
        ReportPeriod resolvedPeriod = period == null ? ReportPeriod.MONTHLY : period;
        LocalDate reference = referenceDate == null ? LocalDate.now() : referenceDate;
        return switch (resolvedPeriod) {
            case DAILY -> new ReportRange(resolvedPeriod, reference, reference);
            case WEEKLY -> {
                LocalDate from = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new ReportRange(resolvedPeriod, from, from.plusDays(6));
            }
            case FORTNIGHTLY -> {
                LocalDate from = reference.getDayOfMonth() <= 15
                        ? reference.withDayOfMonth(1)
                        : reference.withDayOfMonth(16);
                LocalDate to = reference.getDayOfMonth() <= 15
                        ? reference.withDayOfMonth(15)
                        : reference.withDayOfMonth(reference.lengthOfMonth());
                yield new ReportRange(resolvedPeriod, from, to);
            }
            case MONTHLY -> new ReportRange(resolvedPeriod,
                    reference.withDayOfMonth(1),
                    reference.withDayOfMonth(reference.lengthOfMonth()));
        };
    }

    public ReportRange resolveAllowed(ReportPeriod period, LocalDate referenceDate, Set<ReportPeriod> allowed) {
        ReportRange range = resolve(period, referenceDate);
        if (!allowed.contains(range.period())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "El periodo " + range.period() + " no está disponible para este reporte.");
        }
        return range;
    }
}
