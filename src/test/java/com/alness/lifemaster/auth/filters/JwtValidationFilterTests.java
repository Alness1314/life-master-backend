package com.alness.lifemaster.auth.filters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.util.ReflectionTestUtils;

import com.alness.lifemaster.auth.configuration.JwtTokenConfig;
import com.alness.lifemaster.auth.session.RevokedTokenService;
import com.alness.lifemaster.profiles.entity.ProfileEntity;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;

import io.jsonwebtoken.Jwts;

class JwtValidationFilterTests {
    private JwtTokenConfig tokenConfig;
    private UserRepository userRepository;
    private RevokedTokenService revokedTokenService;
    private JwtValidationFilter filter;
    private UUID userId;

    @BeforeEach
    void setUp() {
        tokenConfig = new JwtTokenConfig();
        ReflectionTestUtils.setField(tokenConfig, "secret", "test-secret-key-that-is-long-enough-for-hs256");
        tokenConfig.init();
        userRepository = mock(UserRepository.class);
        revokedTokenService = mock(RevokedTokenService.class);
        filter = new JwtValidationFilter(mock(AuthenticationManager.class), tokenConfig,
                userRepository, revokedTokenService);

        userId = UUID.randomUUID();
        ProfileEntity profile = new ProfileEntity();
        profile.setName("User");
        profile.setErased(false);
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setUsername("user@example.com");
        user.setErased(false);
        user.setProfiles(List.of(profile));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(revokedTokenService.isRevoked("active-session")).thenReturn(false);
    }

    @Test
    void doesNotConvertDownstreamImportErrorsIntoUnauthorizedResponses() {
        MockHttpServletRequest request = request(validToken());
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response,
                (downstreamRequest, downstreamResponse) -> {
                    throw new IllegalArgumentException("No fue posible interpretar el PDF.");
                }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No fue posible interpretar el PDF.");

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void stillRejectsInvalidTokens() throws Exception {
        MockHttpServletRequest request = request("invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (downstreamRequest, downstreamResponse) -> {
            throw new AssertionError("La petición no debe continuar.");
        });

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("sesi");
    }

    @Test
    void swaggerRemainsPublicEvenWhenTheBrowserSendsAnInvalidStoredToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (downstreamRequest, downstreamResponse) -> {
            downstreamResponse.getWriter().write("swagger");
        });

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo("swagger");
        verifyNoInteractions(userRepository, revokedTokenService);
    }

    private MockHttpServletRequest request(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/users/" + userId
                + "/bank-imports/file");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private String validToken() {
        return Jwts.builder()
                .setId("active-session")
                .setSubject("user@example.com")
                .claim("id", userId.toString())
                .setIssuedAt(Date.from(Instant.now().minusSeconds(60)))
                .setExpiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(tokenConfig.getSecretKey())
                .compact();
    }
}
