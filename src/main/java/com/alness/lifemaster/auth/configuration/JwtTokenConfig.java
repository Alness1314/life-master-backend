package com.alness.lifemaster.auth.configuration;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenConfig {
    private Key secretKey;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${api.prefix}")
    private String apiPrefix;

    @PostConstruct
    public void init() {
        log.info("Secret key: {}", secret);
        try {
            secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Error initializing JWT secret key: " + e.getMessage(), e);
        }
    }

    public Key getSecretKey(){
        return this.secretKey;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    
}
