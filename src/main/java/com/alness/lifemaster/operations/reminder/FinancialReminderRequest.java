package com.alness.lifemaster.operations.reminder;

import java.time.LocalDateTime;
import jakarta.validation.constraints.*;

public record FinancialReminderRequest(@NotBlank @Size(max = 256) String title,
        @NotBlank @Size(max = 1024) String message, @NotNull @Future LocalDateTime scheduledAt) {}
