package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;

public record RecurringGenerationResponse(LocalDate through, int generatedExpenses, int generatedIncome) {
}
