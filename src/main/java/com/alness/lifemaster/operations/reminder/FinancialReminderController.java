package com.alness.lifemaster.operations.reminder;

import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/reminders")
@RequiredArgsConstructor
public class FinancialReminderController {
    private final FinancialReminderService service;
    @GetMapping public List<FinancialReminderResponse> findAll(@PathVariable UUID userId) { return service.findAll(userId); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public FinancialReminderResponse save(@PathVariable UUID userId, @Valid @RequestBody FinancialReminderRequest request) {
        return service.save(userId, request);
    }
    @PatchMapping("/{id}/cancel")
    public FinancialReminderResponse cancel(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.cancel(userId, id);
    }
}
