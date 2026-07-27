package com.alness.lifemaster.operations.audit;

import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/audit-events")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('Administrator')")
public class AuditEventController {
    private final AuditEventService service;
    @GetMapping
    public List<AuditEventResponse> find(@RequestParam(required = false) UUID userId,
            @RequestParam(defaultValue = "100") int limit) {
        return service.find(userId, Math.max(1, limit));
    }
}
