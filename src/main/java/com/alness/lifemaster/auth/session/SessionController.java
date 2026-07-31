package com.alness.lifemaster.auth.session;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class SessionController {
    private final RevokedTokenService revokedTokenService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, Authentication authentication) {
        String tokenId = (String) request.getAttribute(JwtSessionAttributes.TOKEN_ID);
        Instant expiresAt = (Instant) request.getAttribute(JwtSessionAttributes.EXPIRES_AT);
        UUID userId = authentication != null && authentication.getDetails() instanceof UUID id ? id : null;

        if (userId != null) {
            revokedTokenService.revoke(tokenId, userId, expiresAt);
        }
        return ResponseEntity.noContent().build();
    }
}
