package com.alness.lifemaster.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class DateTimeUtilsTests {

    @Test
    void acceptsHoursWithOrWithoutSeconds() {
        assertThat(DateTimeUtils.parseToLocalTime("08:30"))
                .isEqualTo(LocalTime.of(8, 30));
        assertThat(DateTimeUtils.parseToLocalTime("18:45:10"))
                .isEqualTo(LocalTime.of(18, 45, 10));
    }

    @Test
    void acceptsApiAndHtmlDateTimeLocalFormats() {
        assertThat(DateTimeUtils.parseToLocalDateTime("2026-07-28 09:30:00"))
                .isEqualTo(LocalDateTime.of(2026, 7, 28, 9, 30));
        assertThat(DateTimeUtils.parseToLocalDateTime("2026-07-28T09:30"))
                .isEqualTo(LocalDateTime.of(2026, 7, 28, 9, 30));
    }
}
