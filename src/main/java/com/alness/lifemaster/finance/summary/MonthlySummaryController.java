package com.alness.lifemaster.finance.summary;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/financial-summary")
@RequiredArgsConstructor
public class MonthlySummaryController {
    private final MonthlySummaryService service;

    @GetMapping("/monthly")
    public MonthlySummaryResponse get(@PathVariable UUID userId, @RequestParam int year, @RequestParam int month,
            @RequestParam(defaultValue = "MXN") String currency) {
        return service.get(userId, year, month, currency);
    }
}
