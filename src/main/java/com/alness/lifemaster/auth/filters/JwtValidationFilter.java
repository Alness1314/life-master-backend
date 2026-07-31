package com.alness.lifemaster.auth.filters;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.alness.lifemaster.auth.configuration.JwtTokenConfig;
import com.alness.lifemaster.auth.dto.KeyPrefix;
import com.alness.lifemaster.auth.session.JwtSessionAttributes;
import com.alness.lifemaster.auth.session.RevokedTokenService;
import com.alness.lifemaster.users.entity.UserEntity;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtValidationFilter extends BasicAuthenticationFilter{
     private final JwtTokenConfig jwtTokenConfig;
     private final UserRepository userRepository;
     private final RevokedTokenService revokedTokenService;

    public JwtValidationFilter(AuthenticationManager authenticationManager, JwtTokenConfig jwtTokenConfig,
            UserRepository userRepository, RevokedTokenService revokedTokenService) {
        super(authenticationManager);
        this.jwtTokenConfig = jwtTokenConfig;
        this.userRepository = userRepository;
        this.revokedTokenService = revokedTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(KeyPrefix.PREFIX_TOKEN)) {
            log.warn("Authorization header is missing or invalid");
            chain.doFilter(request, response);
            return;
        }
        String token = header.replace(KeyPrefix.PREFIX_TOKEN, "");

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtTokenConfig.getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String tokenId = claims.getId();
            if (tokenId == null || tokenId.isBlank() || revokedTokenService.isRevoked(tokenId)) {
                throw new JwtException("La sesión fue revocada o pertenece a una versión anterior.");
            }

            UUID userId = UUID.fromString(claims.get("id", String.class));
            UserEntity user = userRepository.findById(userId)
                    .filter(value -> !Boolean.TRUE.equals(value.getErased()))
                    .orElseThrow(() -> new JwtException("El usuario de la sesión ya no está activo."));
            if (!user.getUsername().equalsIgnoreCase(claims.getSubject())) {
                throw new JwtException("La identidad de la sesión ya no coincide con el usuario.");
            }

            Collection<SimpleGrantedAuthority> authorities = user.getProfiles().stream()
                    .filter(profile -> !Boolean.TRUE.equals(profile.getErased()))
                    .map(profile -> new SimpleGrantedAuthority(profile.getName()))
                    .toList();
            if (authorities.isEmpty()) {
                throw new JwtException("El usuario ya no tiene perfiles activos.");
            }

            UsernamePasswordAuthenticationToken autentication = new UsernamePasswordAuthenticationToken(
                    user.getUsername(), null, authorities);
            autentication.setDetails(userId);

            SecurityContextHolder.getContext().setAuthentication(autentication);
            request.setAttribute(JwtSessionAttributes.TOKEN_ID, tokenId);
            request.setAttribute(JwtSessionAttributes.EXPIRES_AT, claims.getExpiration().toInstant());
        } catch (ExpiredJwtException e) {
            handleError(response, "La sesión expiró.", e, HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (JwtException | IllegalArgumentException e) {
            handleError(response, "La sesión no es válida o fue revocada.", e,
                    HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            chain.doFilter(request, response);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    private void handleError(HttpServletResponse response, String message, Exception e, int status) throws IOException {
        log.warn("{}: {}", message, e.getMessage());
        Map<String, String> bodyResponse = new HashMap<>();
        bodyResponse.put("code", ApiCodes.API_CODE + status);
            bodyResponse.put("error", "Unauthorized");
            bodyResponse.put("message", message);
            response.setStatus(status);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));
    }
}
