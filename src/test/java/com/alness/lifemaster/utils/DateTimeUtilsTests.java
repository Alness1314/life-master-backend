package com.alness.lifemaster.utils;

import static org.assertj.core.api.Assertions.assertThat;

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
}
