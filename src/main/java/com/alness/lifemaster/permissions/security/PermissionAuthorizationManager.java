package com.alness.lifemaster.permissions.security;

import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import com.alness.lifemaster.common.enums.AllowedProfiles;
import com.alness.lifemaster.permissions.service.EffectivePermissionService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PermissionAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final PermissionRouteResolver routeResolver;
    private final EffectivePermissionService effectivePermissionService;

    @Override
    public AuthorizationDecision check(
            Supplier<Authentication> authenticationSupplier,
            RequestAuthorizationContext context) {
        Authentication authentication = authenticationSupplier.get();
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> AllowedProfiles.ADMIN.getName().equals(authority.getAuthority()))) {
            return new AuthorizationDecision(true);
        }
        if (!(authentication.getDetails() instanceof UUID userId)) {
            return new AuthorizationDecision(false);
        }

        return routeResolver.resolve(context.getRequest())
                .map(target -> new AuthorizationDecision(effectivePermissionService.hasPermission(
                        userId, target.permissionKey(), target.action())))
                .orElseGet(() -> new AuthorizationDecision(false));
    }
}
