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
    private static final java.util.regex.Pattern UUID_PATTERN = java.util.regex.Pattern.compile(
            "([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})");

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
            if (shouldAudit(request)) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                UUID userId = auth != null && auth.getDetails() instanceof UUID id ? id : null;
                String resource = request.getRequestURI();
                String module = resolveModule(resource);
                String action = resolveAction(request.getMethod());
                String recordId = resolveRecordId(resource, userId);
                String detail = action + " en " + module + " (" + response.getStatus() + ")";
                try {
                    service.record(userId, auth == null ? null : auth.getName(), request.getMethod(),
                            resource, action, module, recordId, detail, response.getStatus(), correlationId,
                            clientIp(request), truncate(request.getHeader("User-Agent"), 512),
                            (System.nanoTime() - started) / 1_000_000);
                } catch (RuntimeException exception) {
                    LOGGER.error("Could not persist audit event {}", correlationId, exception);
                }
            }
            MDC.remove("correlationId");
        }
    }

    private boolean shouldAudit(HttpServletRequest request) {
        if (MUTATIONS.contains(request.getMethod())) {
            return true;
        }
        if (!"GET".equals(request.getMethod())) {
            return false;
        }
        String resource = request.getRequestURI();
        return resource.matches(".*/files/[0-9a-fA-F-]{36}/content$")
                || resource.matches(".*/receipts/[0-9a-fA-F-]{36}/content$")
                || resource.matches(".*/vault/[0-9a-fA-F-]{36}/password$");
    }

    private String resolveAction(String method) {
        return switch (method) {
            case "POST" -> "CREAR";
            case "PUT", "PATCH" -> "ACTUALIZAR";
            case "DELETE" -> "ELIMINAR";
            default -> "CONSULTAR";
        };
    }

    private String resolveModule(String resource) {
        String[] parts = resource.split("/");
        for (int index = 0; index < parts.length; index++) {
            if ("users".equals(parts[index]) && index + 2 < parts.length) {
                return normalize(parts[index + 2]);
            }
        }
        for (String part : parts) {
            if (!part.isBlank() && !"api".equals(part) && !part.matches("v\\d+")
                    && !"users".equals(part) && !UUID_PATTERN.matcher(part).matches()) {
                return normalize(part);
            }
        }
        return "SISTEMA";
    }

    private String normalize(String value) {
        return value.replace('-', '_').toUpperCase(Locale.ROOT);
    }

    private String resolveRecordId(String resource, UUID authenticatedUserId) {
        java.util.regex.Matcher matcher = UUID_PATTERN.matcher(resource);
        String last = null;
        while (matcher.find()) {
            String value = matcher.group(1);
            if (authenticatedUserId == null || !authenticatedUserId.toString().equalsIgnoreCase(value)) {
                last = value;
            }
        }
        return last;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return truncate(forwarded.split(",")[0].trim(), 64);
        }
        return truncate(request.getRemoteAddr(), 64);
    }

    private String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
