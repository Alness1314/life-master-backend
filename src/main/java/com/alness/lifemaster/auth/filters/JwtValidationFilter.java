package com.alness.lifemaster.auth.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.alness.lifemaster.auth.configuration.JwtTokenConfig;
import com.alness.lifemaster.auth.configuration.SimpleGrantedAuthorityJsonCreator;
import com.alness.lifemaster.auth.dto.KeyPrefix;
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
     private static final String AUTHORITIES_KEY = "authorities";
     private final JwtTokenConfig jwtTokenConfig;

    public JwtValidationFilter(AuthenticationManager authenticationManager, JwtTokenConfig jwtTokenConfig) {
        super(authenticationManager);
        this.jwtTokenConfig = jwtTokenConfig;
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

            Object authoritiesClaims = claims.get(AUTHORITIES_KEY);

            Collection<SimpleGrantedAuthority> authorities = Arrays.asList(new ObjectMapper()
                    .addMixIn(SimpleGrantedAuthority.class, SimpleGrantedAuthorityJsonCreator.class)
                    .readValue(authoritiesClaims.toString().getBytes(), SimpleGrantedAuthority[].class));

            UsernamePasswordAuthenticationToken autentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);

            SecurityContextHolder.getContext().setAuthentication(autentication);
            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleError(response, "Token expired", e, HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtException e) {
            handleError(response, "Invalid JWT token", e, HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException e) {
            handleError(response, "IO Error while processing JWT", e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            SecurityContextHolder.clearContext();
        }

    }

    private void handleError(HttpServletResponse response, String message, Exception e, int status) throws IOException {
        log.error(message, e);
        Map<String, String> bodyResponse = new HashMap<>();
        bodyResponse.put("code", ApiCodes.API_CODE + status);
        bodyResponse.put("error", e.getMessage());
        bodyResponse.put("message", message);
        response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
