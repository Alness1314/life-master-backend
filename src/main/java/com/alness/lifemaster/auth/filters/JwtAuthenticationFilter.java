package com.alness.lifemaster.auth.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.alness.lifemaster.auth.configuration.JwtTokenConfig;
import com.alness.lifemaster.auth.dto.AuthenticationDto;
import com.alness.lifemaster.auth.dto.KeyPrefix;
import com.alness.lifemaster.common.enums.AllowedProfiles;
import com.alness.lifemaster.users.dto.CustomUser;
import com.alness.lifemaster.utils.ApiCodes;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenConfig jwtTokenConfig;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenConfig jwtTokenConfig) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenConfig = jwtTokenConfig;
        setFilterProcessesUrl(jwtTokenConfig.getApiPrefix() + "/auth");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        AuthenticationDto user;
        try {
            user = new ObjectMapper().readValue(request.getInputStream(), AuthenticationDto.class);
            log.info("User attempting login: {}", user);

            if (user.getUsername() == null || user.getUsername().isEmpty() ||
                    user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new AuthenticationException("Username or password cannot be empty") {
                };
            }
        } catch (IOException e) {
            log.error("Error reading authentication request: {}", e.getMessage(), e);
            throw new AuthenticationException("Invalid login request") {
            };
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user.getUsername(),
                user.getPassword());
        return authenticationManager.authenticate(authToken);

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        CustomUser customUser = (CustomUser) authResult.getPrincipal();
        String username = customUser.getUsername();
        UUID userId = customUser.getUserId();

        Collection<? extends GrantedAuthority> profiles = authResult.getAuthorities();
        boolean isAdmin = profiles.stream().anyMatch(res -> res.getAuthority().equals(AllowedProfiles.ADMIN.getName()));

        Claims claims = Jwts.claims();
        claims.put("authorities", new ObjectMapper().writeValueAsString(profiles));
        claims.put("admin", isAdmin);
        claims.put("id", userId);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .signWith(jwtTokenConfig.getSecretKey())
                .setIssuedAt(new Date())
                .setExpiration(new Date(this.getTimeToken(24L))) // expiracion en 24 horas
                .compact();

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(KeyPrefix.CHAR_ENCODING);
        response.setStatus(202);
        response.addHeader(HttpHeaders.AUTHORIZATION, KeyPrefix.PREFIX_TOKEN + token);

        Map<String, Object> bodyResponse = new HashMap<>();
        bodyResponse.put("token", token);
        bodyResponse.put("message", "I log in with a valid user.");
        bodyResponse.put("code", ApiCodes.API_CODE_202);
        response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(KeyPrefix.CHAR_ENCODING);
        response.setStatus(401);

        Map<String, Object> bodyResponse = new HashMap<>();
        bodyResponse.put("code", ApiCodes.API_CODE_401);
        bodyResponse.put("message", "Invalid username or password.");
        bodyResponse.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));

    }

    private Long getTimeToken(Long hour) {
        return System.currentTimeMillis() + (hour * 60 * 60 * 1000);
    }
}
