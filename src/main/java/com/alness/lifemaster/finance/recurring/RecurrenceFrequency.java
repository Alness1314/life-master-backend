package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.time.YearMonth;

public enum RecurrenceFrequency {
    WEEKLY {
        public LocalDate next(LocalDate date) { return date.plusWeeks(1); }
    },
    MONTHLY {
        public LocalDate next(LocalDate date) { return date.plusMonths(1); }
    },
    YEARLY {
        public LocalDate next(LocalDate date) { return date.plusYears(1); }
    };

    public abstract LocalDate next(LocalDate date);

    public LocalDate next(LocalDate date, LocalDate anchor) {
        if (this != MONTHLY) {
            return next(date);
        }
        YearMonth nextMonth = YearMonth.from(date).plusMonths(1);
        return nextMonth.atDay(Math.min(anchor.getDayOfMonth(), nextMonth.lengthOfMonth()));
    }
}
