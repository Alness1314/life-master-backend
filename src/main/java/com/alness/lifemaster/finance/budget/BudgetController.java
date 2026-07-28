package com.alness.lifemaster.finance.budget;

import java.util.List;
import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService service;

    @GetMapping
    public List<BudgetResponse> findPeriod(@PathVariable UUID userId, @RequestParam int year,
            @RequestParam int month) {
        return service.findPeriod(userId, year, month);
    }

    @GetMapping("/{id}")
    public BudgetResponse findOne(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.findOne(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse save(@PathVariable UUID userId, @Valid @RequestBody BudgetRequest request) {
        return service.save(userId, request);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable UUID userId, @PathVariable UUID id,
            @Valid @RequestBody BudgetRequest request) {
        return service.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
