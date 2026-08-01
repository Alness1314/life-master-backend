package com.alness.lifemaster.permissions.security;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.permissions.PermissionAction;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class PermissionRouteResolver {
    private final String apiPrefix;
    private final List<RoutePermission> routes = List.of(
            route("/category(?:/.*)?", "categories"),
            route("/users/[^/]+/expenses(?:/[^/]+/receipts(?:/.*)?)?", "expenses"),
            route("/users/[^/]+/expenses(?:/.*)?", "expenses"),
            route("/users/[^/]+/income(?:/.*)?", "incomes"),
            route("/users/[^/]+/debts(?:/.*)?", "debts"),
            route("/users/[^/]+/accounts(?:/.*)?", "financial-accounts"),
            route("/users/[^/]+/payment-methods(?:/.*)?", "payment-methods"),
            route("/users/[^/]+/recurring-movements(?:/.*)?", "recurring-movements"),
            route("/users/[^/]+/financial-summary(?:/.*)?", "dashboard"),
            route("/users/[^/]+/alerts(?:/.*)?", "alerts"),
            route("/users/[^/]+/reminders(?:/.*)?", "reminders"),
            route("/users/[^/]+/bank-imports(?:/.*)?", "bank-import"),
            route("/users/[^/]+/reports(?:/.*)?", "reports"),
            route("/users/[^/]+/files(?:/.*)?", "files"),
            route("/users/[^/]+/vault(?:/.*)?", "vault"),
            route("/users/[^/]+/notes(?:/.*)?", "notes"),
            route("/users/[^/]+/nutrition(?:/.*)?", "nutrition"),
            route("/users/[^/]+/exercises(?:/.*)?", "exercises"),
            route("/users/[^/]+/assistance(?:/.*)?", "asistencia"),
            route("/audit-events(?:/.*)?", "audit"),
            route("/profiles(?:/.*)?", "users"),
            route("/modules(?:/.*)?", "app-modules"),
            route("/permissions(?:/.*)?", "app-modules"),
            route("/users(?:/.*)?", "users"));

    public PermissionRouteResolver(@Value("${api.prefix}") String apiPrefix) {
        this.apiPrefix = normalizePrefix(apiPrefix);
    }

    public Optional<PermissionTarget> resolve(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith(apiPrefix)) {
            return Optional.empty();
        }
        String apiPath = uri.substring(apiPrefix.length());
        PermissionAction action = actionFor(request.getMethod());
        return routes.stream()
                .filter(route -> route.pattern().matcher(apiPath).matches())
                .findFirst()
                .map(route -> new PermissionTarget(route.permissionKey(), action));
    }

    private PermissionAction actionFor(String method) {
        if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method)) {
            return PermissionAction.READ;
        }
        if (HttpMethod.POST.matches(method)) {
            return PermissionAction.CREATE;
        }
        if (HttpMethod.DELETE.matches(method)) {
            return PermissionAction.DELETE;
        }
        return PermissionAction.UPDATE;
    }

    private RoutePermission route(String expression, String permissionKey) {
        return new RoutePermission(Pattern.compile(expression), permissionKey);
    }

    private String normalizePrefix(String value) {
        String normalized = value == null || value.isBlank() ? "/api/v1" : value.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private record RoutePermission(Pattern pattern, String permissionKey) {
    }
}
