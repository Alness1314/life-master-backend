package com.alness.lifemaster.operations.audit;

import java.util.List;

public record AuditEventPageResponse(List<AuditEventResponse> content, long totalElements,
        int totalPages, int page, int size) {
}
