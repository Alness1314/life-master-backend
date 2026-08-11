package com.alness.lifemaster.swagger;

import java.util.List;

public final class SwaggerPaths {
    private static final List<String> SECURITY_MATCHERS = List.of(
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/api-docs",
            "/api-docs/**",
            "/api-docs.yaml",
            "/v3/api-docs",
            "/v3/api-docs/**");

    private SwaggerPaths() {
    }

    public static String[] securityMatchers() {
        return SECURITY_MATCHERS.toArray(String[]::new);
    }

    public static boolean isDocumentationRequest(String requestUri) {
        return requestUri != null && (
                requestUri.equals("/swagger-ui.html")
                        || requestUri.startsWith("/swagger-ui/")
                        || requestUri.equals("/api-docs")
                        || requestUri.startsWith("/api-docs/")
                        || requestUri.equals("/api-docs.yaml")
                        || requestUri.equals("/v3/api-docs")
                        || requestUri.startsWith("/v3/api-docs/"));
    }
}
