package com.alness.lifemaster.finance.recurring;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/recurring-movements")
@RequiredArgsConstructor
public class RecurringMovementController {
    private final RecurringMovementService service;

    @GetMapping
    public List<RecurringMovementResponse> findAll(@PathVariable UUID userId) {
        return service.findAll(userId);
    }

    @GetMapping("/{id}")
    public RecurringMovementResponse findOne(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.findOne(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringMovementResponse save(@PathVariable UUID userId,
            @Valid @RequestBody RecurringMovementRequest request) {
        return service.save(userId, request);
    }

    @PutMapping("/{id}")
    public RecurringMovementResponse update(@PathVariable UUID userId, @PathVariable UUID id,
            @Valid @RequestBody RecurringMovementRequest request) {
        return service.update(userId, id, request);
    }

    @PostMapping("/generate")
    public RecurringGenerationResponse generate(@PathVariable UUID userId,
            @RequestParam(required = false) LocalDate through) {
        return service.generateDue(userId, through == null ? LocalDate.now() : through);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
