package com.alness.lifemaster.operations.audit;

import java.io.IOException;
import java.util.*;
import org.slf4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class AuditFilter extends OncePerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuditFilter.class);
    private static final Set<String> MUTATIONS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final AuditEventService service;

    public AuditFilter(AuditEventService service) {
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = Optional.ofNullable(request.getHeader("X-Correlation-ID"))
                .filter(value -> value.matches("^[A-Za-z0-9-]{8,64}$"))
                .orElseGet(() -> UUID.randomUUID().toString());
        response.setHeader("X-Correlation-ID", correlationId);
        long started = System.nanoTime();
        MDC.put("correlationId", correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            if (MUTATIONS.contains(request.getMethod())) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                UUID userId = auth != null && auth.getDetails() instanceof UUID id ? id : null;
                try {
                    service.record(userId, auth == null ? null : auth.getName(), request.getMethod(),
                            request.getRequestURI(), response.getStatus(), correlationId, request.getRemoteAddr(),
                            (System.nanoTime() - started) / 1_000_000);
                } catch (RuntimeException exception) {
                    LOGGER.error("Could not persist audit event {}", correlationId, exception);
                }
            }
            MDC.remove("correlationId");
        }
    }
}
