package com.alness.lifemaster.operations.audit;

import java.util.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/audit-events")
@RequiredArgsConstructor
public class AuditEventController {
    private final AuditEventService service;
    @GetMapping
    public List<AuditEventResponse> find(@RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "100") int limit) {
        return service.find(userId, Math.max(1, limit));
    }

    @GetMapping("/search")
    public AuditEventPageResponse search(@RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String recordId,
            @RequestParam(required = false) Boolean successful,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.search(userId, username, module, action, recordId, successful, from, to, page, size);
    }

    @GetMapping("/{id}")
    public AuditEventResponse findOne(@PathVariable UUID id) {
        return service.findOne(id);
    }
}
