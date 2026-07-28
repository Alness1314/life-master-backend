package com.alness.lifemaster.finance.paymentmethod;

import java.util.List;
import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/payment-methods")
@RequiredArgsConstructor
public class PaymentMethodController {
    private final PaymentMethodService service;

    @GetMapping
    public List<PaymentMethodResponse> findAll(@PathVariable UUID userId) {
        return service.findAll(userId);
    }

    @GetMapping("/{id}")
    public PaymentMethodResponse findOne(@PathVariable UUID userId, @PathVariable UUID id) {
        return service.findOne(userId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentMethodResponse save(@PathVariable UUID userId, @Valid @RequestBody PaymentMethodRequest request) {
        return service.save(userId, request);
    }

    @PutMapping("/{id}")
    public PaymentMethodResponse update(@PathVariable UUID userId, @PathVariable UUID id,
            @Valid @RequestBody PaymentMethodRequest request) {
        return service.update(userId, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID id) {
        service.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}
