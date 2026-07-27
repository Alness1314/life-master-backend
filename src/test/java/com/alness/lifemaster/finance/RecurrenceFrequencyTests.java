package com.alness.lifemaster.finance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.alness.lifemaster.finance.recurring.RecurrenceFrequency;

class RecurrenceFrequencyTests {
    @Test
    void monthlyRecurrenceUsesCalendarDateSemantics() {
        assertThat(RecurrenceFrequency.MONTHLY.next(LocalDate.of(2026, 1, 31)))
                .isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(RecurrenceFrequency.MONTHLY.next(
                LocalDate.of(2026, 2, 28), LocalDate.of(2026, 1, 31)))
                .isEqualTo(LocalDate.of(2026, 3, 31));
    }
}
