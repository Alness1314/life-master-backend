package com.alness.lifemaster.operations.alert;

import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialAlertResponse(UUID id, String type, String severity, String title, String message,
        Boolean read, LocalDateTime createdAt) {}
