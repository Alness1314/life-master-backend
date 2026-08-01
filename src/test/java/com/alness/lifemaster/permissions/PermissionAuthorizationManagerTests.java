package com.alness.lifemaster.permissions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.alness.lifemaster.permissions.security.PermissionAuthorizationManager;
import com.alness.lifemaster.permissions.security.PermissionRouteResolver;
import com.alness.lifemaster.permissions.service.EffectivePermissionService;

class PermissionAuthorizationManagerTests {
    private final EffectivePermissionService permissions = mock(EffectivePermissionService.class);
    private final PermissionAuthorizationManager manager = new PermissionAuthorizationManager(
            new PermissionRouteResolver("/api/v1"), permissions);

    @Test
    void delegatesUserAuthorizationToPersistedCrudPermissions() {
        UUID userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken authentication = authentication(userId, "User");
        when(permissions.hasPermission(userId, "categories", PermissionAction.CREATE)).thenReturn(true);

        boolean granted = manager.authorize(
                () -> authentication,
                context("POST", "/api/v1/category")).isGranted();

        assertThat(granted).isTrue();
        verify(permissions).hasPermission(userId, "categories", PermissionAction.CREATE);
    }

    @Test
    void administratorBypassesPermissionRowsForRecoveryAndBootstrap() {
        UsernamePasswordAuthenticationToken authentication = authentication(UUID.randomUUID(), "Administrator");

        boolean granted = manager.authorize(
                () -> authentication,
                context("DELETE", "/api/v1/permissions/profiles/a/modules/b")).isGranted();

        assertThat(granted).isTrue();
        verifyNoInteractions(permissions);
    }

    @Test
    void deniesUnmappedApiRoutesByDefault() {
        UsernamePasswordAuthenticationToken authentication = authentication(UUID.randomUUID(), "User");

        boolean granted = manager.authorize(
                () -> authentication,
                context("GET", "/api/v1/unmapped-resource")).isGranted();

        assertThat(granted).isFalse();
        verifyNoInteractions(permissions);
    }

    private UsernamePasswordAuthenticationToken authentication(UUID userId, String authority) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(new SimpleGrantedAuthority(authority)));
        authentication.setDetails(userId);
        return authentication;
    }

    private RequestAuthorizationContext context(String method, String uri) {
        return new RequestAuthorizationContext(new MockHttpServletRequest(method, uri));
    }
}
