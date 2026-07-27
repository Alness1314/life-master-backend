package com.alness.lifemaster.operations.receipt;

import java.util.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/expenses/{expenseId}/receipts")
@RequiredArgsConstructor
public class ExpenseReceiptController {
    private final ExpenseReceiptService service;

    @GetMapping
    public List<ExpenseReceiptResponse> findAll(@PathVariable UUID userId, @PathVariable UUID expenseId) {
        return service.findAll(userId, expenseId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseReceiptResponse save(@PathVariable UUID userId, @PathVariable UUID expenseId,
            @RequestPart("file") MultipartFile file) {
        return service.save(userId, expenseId, file);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> download(@PathVariable UUID userId, @PathVariable UUID expenseId,
            @PathVariable UUID id) {
        ExpenseReceiptEntity value = service.download(userId, id);
        if (!value.getExpense().getId().equals(expenseId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + value.getOriginalName().replace("\"", "") + "\"")
                .contentType(MediaType.parseMediaType(value.getContentType()))
                .body(value.getContent());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, @PathVariable UUID expenseId,
            @PathVariable UUID id) {
        service.delete(userId, expenseId, id);
        return ResponseEntity.noContent().build();
    }
}
