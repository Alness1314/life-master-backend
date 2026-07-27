package com.alness.lifemaster.operations.reminder;

import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialReminderResponse(UUID id, String title, String message, LocalDateTime scheduledAt,
        Boolean delivered, Boolean cancelled) {}
