package com.alness.lifemaster.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.alness.lifemaster.app.dto.ResponseServer;
import com.alness.lifemaster.app.service.impl.AppConfigServiceImpl;
import com.alness.lifemaster.profiles.service.ProfileService;
import com.alness.lifemaster.users.service.UserService;

@ExtendWith(MockitoExtension.class)
class AppConfigSessionTests {

    @Mock
    private ProfileService profileService;
    @Mock
    private UserService userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAcceptedForTheIdentityAlreadyValidatedByTheSecurityFilter() {
        UUID userId = UUID.randomUUID();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("user@example.com", null, List.of());
        authentication.setDetails(userId);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer signed-token");

        ResponseServer response = service().checkStatusSession(request);

        assertThat(response.getCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getStatus()).isTrue();
        assertThat(response.getData())
                .containsEntry("id", userId)
                .containsEntry("username", "user@example.com")
                .containsEntry("token", "signed-token");
    }

    @Test
    void returnsUnauthorizedWhenThereIsNoAuthenticatedIdentity() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer signed-token");

        ResponseServer response = service().checkStatusSession(request);

        assertThat(response.getCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getStatus()).isFalse();
    }

    private AppConfigServiceImpl service() {
        return new AppConfigServiceImpl(profileService, userService);
    }
}
