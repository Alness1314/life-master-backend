package com.alness.lifemaster.finance.summary;

import java.math.BigDecimal;
import java.util.UUID;

public record CategoryExpenseSummary(UUID categoryId, String categoryName, BigDecimal amount) {
}
