package com.alness.lifemaster.operations.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.exceptions.RestExceptionHandler;

class ReportPeriodResolverTests {
    private final ReportPeriodResolver resolver = new ReportPeriodResolver();

    @Test
    void resolvesCustomRangeAndRejectsInvertedDates() {
        ReportRange range = resolver.resolve(ReportPeriod.CUSTOM, null,
                LocalDate.of(2026, 7, 20), LocalDate.of(2026, 8, 12));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 12));
        assertThatThrownBy(() -> resolver.resolve(ReportPeriod.CUSTOM, null,
                LocalDate.of(2026, 8, 13), LocalDate.of(2026, 8, 12)))
                .isInstanceOf(RestExceptionHandler.class);
    }

    @Test
    void resolvesIsoWeekFromMondayToSunday() {
        ReportRange range = resolver.resolve(ReportPeriod.WEEKLY, LocalDate.of(2026, 8, 11));

        assertThat(range.from()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(range.to()).isEqualTo(LocalDate.of(2026, 8, 16));
    }

    @Test
    void resolvesBothFortnightsUsingCalendarMonthBoundaries() {
        ReportRange first = resolver.resolve(ReportPeriod.FORTNIGHTLY, LocalDate.of(2026, 2, 15));
        ReportRange second = resolver.resolve(ReportPeriod.FORTNIGHTLY, LocalDate.of(2028, 2, 16));

        assertThat(first.from()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(first.to()).isEqualTo(LocalDate.of(2026, 2, 15));
        assertThat(second.from()).isEqualTo(LocalDate.of(2028, 2, 16));
        assertThat(second.to()).isEqualTo(LocalDate.of(2028, 2, 29));
    }

    @Test
    void rejectsUnsupportedPeriodForModule() {
        assertThatThrownBy(() -> resolver.resolveAllowed(ReportPeriod.DAILY, LocalDate.now(),
                Set.of(ReportPeriod.WEEKLY)))
                .isInstanceOf(RestExceptionHandler.class)
                .hasMessageContaining("no está disponible");
    }
}
