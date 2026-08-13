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
        return resolve(period, referenceDate, null, null);
    }

    public ReportRange resolve(ReportPeriod period, LocalDate referenceDate, LocalDate from, LocalDate to) {
        ReportPeriod resolvedPeriod = period == null ? ReportPeriod.MONTHLY : period;
        LocalDate reference = referenceDate == null ? LocalDate.now() : referenceDate;
        return switch (resolvedPeriod) {
            case DAILY -> new ReportRange(resolvedPeriod, reference, reference);
            case WEEKLY -> {
                LocalDate weekFrom = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new ReportRange(resolvedPeriod, weekFrom, weekFrom.plusDays(6));
            }
            case FORTNIGHTLY -> {
                LocalDate fortnightFrom = reference.getDayOfMonth() <= 15
                        ? reference.withDayOfMonth(1)
                        : reference.withDayOfMonth(16);
                LocalDate fortnightTo = reference.getDayOfMonth() <= 15
                        ? reference.withDayOfMonth(15)
                        : reference.withDayOfMonth(reference.lengthOfMonth());
                yield new ReportRange(resolvedPeriod, fortnightFrom, fortnightTo);
            }
            case MONTHLY -> new ReportRange(resolvedPeriod,
                    reference.withDayOfMonth(1),
                    reference.withDayOfMonth(reference.lengthOfMonth()));
            case CUSTOM -> customRange(from, to);
        };
    }

    public ReportRange resolveAllowed(ReportPeriod period, LocalDate referenceDate, Set<ReportPeriod> allowed) {
        return resolveAllowed(period, referenceDate, null, null, allowed);
    }

    public ReportRange resolveAllowed(ReportPeriod period, LocalDate referenceDate,
            LocalDate from, LocalDate to, Set<ReportPeriod> allowed) {
        ReportRange range = resolve(period, referenceDate, from, to);
        if (!allowed.contains(range.period())) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "El periodo " + range.period() + " no está disponible para este reporte.");
        }
        return range;
    }

    private ReportRange customRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "Las fechas desde y hasta son obligatorias para un periodo personalizado.");
        }
        if (from.isAfter(to)) {
            throw new RestExceptionHandler(ApiCodes.API_CODE_400, HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta.");
        }
        return new ReportRange(ReportPeriod.CUSTOM, from, to);
    }
}
