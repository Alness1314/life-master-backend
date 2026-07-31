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
    private final BankImportTemplateService templateService;

    @GetMapping(value = "/template.csv", produces = "text/csv")
    public ResponseEntity<byte[]> downloadCsvTemplate(@PathVariable UUID userId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"plantilla-importacion-bancaria.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(templateService.csv());
    }

    @GetMapping(value = "/template.xlsx",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> downloadExcelTemplate(@PathVariable UUID userId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"plantilla-importacion-bancaria.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(templateService.excel());
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BankImportResponse importFile(@PathVariable UUID userId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID expenseCategoryId,
            @RequestParam(defaultValue = "MXN") String defaultCurrency,
            @RequestParam(defaultValue = "true") boolean dryRun,
            @RequestPart("file") MultipartFile file) {
        return service.importFile(userId, accountId, expenseCategoryId, defaultCurrency, file, dryRun);
    }

    /**
     * Conserva el contrato original para clientes que todavía envían CSV.
     */
    @PostMapping(value = "/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BankImportResponse importCsv(@PathVariable UUID userId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) UUID expenseCategoryId,
            @RequestParam(defaultValue = "true") boolean dryRun,
            @RequestPart("file") MultipartFile file) {
        return service.importFile(userId, accountId, expenseCategoryId, "MXN", file, dryRun);
    }
}
