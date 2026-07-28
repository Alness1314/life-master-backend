package com.alness.lifemaster.finance.account;

import java.util.List;
import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/accounts")
@RequiredArgsConstructor
public class FinancialAccountController {
    private final FinancialAccountService service;

    @GetMapping
    public List<FinancialAccountResponse> findAll(@PathVariable UUID userId) {
        return service.findAll(userId);
    }

    @GetMapping("/{id}")
    public FinancialAccountResponse findOne(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.findOne(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialAccountResponse save(@PathVariable UUID userId, @Valid @RequestBody FinancialAccountRequest request) {
        return service.save(userId, request);
    }

    @PutMapping("/{id}")
    public FinancialAccountResponse update(@PathVariable UUID userId, @PathVariable UUID id,
            @Valid @RequestBody FinancialAccountRequest request) {
        return service.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
