package com.alness.lifemaster.operations.report;

import java.util.UUID;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/users/{userId}/reports")
@RequiredArgsConstructor
public class FinancialReportController {
    private final FinancialReportService service;

    @GetMapping(value = "/monthly.csv", produces = "text/csv")
    public ResponseEntity<byte[]> monthlyCsv(@PathVariable UUID userId, @RequestParam int year,
            @RequestParam int month, @RequestParam(defaultValue = "MXN") String currency) {
        byte[] content = service.monthlyCsv(userId, year, month, currency);
        String fileName = "life-master-" + year + "-" + String.format("%02d", month) + "-" + currency + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(content);
    }
}
