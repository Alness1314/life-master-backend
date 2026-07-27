package com.alness.lifemaster.auth.filters;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.alness.lifemaster.common.enums.AllowedProfiles;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UserOwnershipFilter extends OncePerRequestFilter {
    private static final Pattern USER_RESOURCE = Pattern.compile("/users/([0-9a-fA-F-]{36})(?:/|$)");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Matcher matcher = USER_RESOURCE.matcher(request.getRequestURI());
        if (!matcher.find()) {
            chain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> AllowedProfiles.ADMIN.getName().equals(authority.getAuthority()));
        UUID authenticatedUserId = authentication != null && authentication.getDetails() instanceof UUID id ? id : null;

        if (!admin && (authenticatedUserId == null || !authenticatedUserId.toString().equalsIgnoreCase(matcher.group(1)))) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    java.util.Map.of("code", "API-403", "error", "Forbidden",
                            "message", "You cannot access another user's resources.")));
            return;
        }
        chain.doFilter(request, response);
    }
}
