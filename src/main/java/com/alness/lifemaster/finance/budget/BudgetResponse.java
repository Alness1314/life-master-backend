package com.alness.lifemaster.finance.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetResponse(UUID id, UUID categoryId, String categoryName, Integer year, Integer month,
        String currency, BigDecimal amount, BigDecimal spent, BigDecimal remaining, BigDecimal usagePercentage,
        Integer alertPercentage, boolean alert, boolean exceeded) {
}
