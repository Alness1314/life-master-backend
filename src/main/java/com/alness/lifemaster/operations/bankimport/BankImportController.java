package com.alness.lifemaster.operations.bankimport;

import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/bank-imports")
@RequiredArgsConstructor
public class BankImportController {
    private final BankImportService service;

    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BankImportResponse importCsv(@PathVariable UUID userId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID expenseCategoryId,
            @RequestParam(defaultValue = "true") boolean dryRun,
            @RequestPart("file") MultipartFile file) {
        return service.importCsv(userId, accountId, expenseCategoryId, file, dryRun);
    }
}
