package com.alness.lifemaster.operations.alert;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/alerts")
@RequiredArgsConstructor
public class FinancialAlertController {
    private final FinancialAlertService service;

    @GetMapping
    public List<FinancialAlertResponse> findAll(@PathVariable UUID userId) {
        return service.findAll(userId);
    }

    @PostMapping("/refresh")
    public List<FinancialAlertResponse> refresh(@PathVariable UUID userId) {
        return service.refresh(userId);
    }

    @PatchMapping("/{id}/read")
    public FinancialAlertResponse read(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.markRead(userId, id);
    }
}
