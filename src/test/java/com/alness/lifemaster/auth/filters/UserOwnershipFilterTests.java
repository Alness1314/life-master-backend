package com.alness.lifemaster.auth.filters;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class UserOwnershipFilterTests {
    private final UserOwnershipFilter filter = new UserOwnershipFilter();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsAuthenticatedUserToAccessOwnResources() throws Exception {
        UUID userId = UUID.randomUUID();
        authenticate(userId, "User");
        MockHttpServletResponse response = execute("/api/v1/users/" + userId + "/expenses");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void rejectsAccessToAnotherUsersResources() throws Exception {
        authenticate(UUID.randomUUID(), "User");
        MockHttpServletResponse response = execute("/api/v1/users/" + UUID.randomUUID() + "/expenses");

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("another user's resources");
    }

    @Test
    void allowsAdministratorToAccessManagedUserResources() throws Exception {
        authenticate(UUID.randomUUID(), "Administrator");
        MockHttpServletResponse response = execute("/api/v1/users/" + UUID.randomUUID() + "/expenses");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    private void authenticate(UUID userId, String authority) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "user@example.com", null, List.of(new SimpleGrantedAuthority(authority)));
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private MockHttpServletResponse execute(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
