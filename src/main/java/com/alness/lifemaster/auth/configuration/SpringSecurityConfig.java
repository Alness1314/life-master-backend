package com.alness.lifemaster.auth.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.alness.lifemaster.auth.filters.JwtAuthenticationFilter;
import com.alness.lifemaster.auth.filters.JwtValidationFilter;
import com.alness.lifemaster.utils.ApiCodes;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;


@Configuration
public class SpringSecurityConfig {
    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    private final JwtTokenConfig jwtTokenConfig;

    public SpringSecurityConfig(JwtTokenConfig jwtTokenConfig) {
        this.jwtTokenConfig = jwtTokenConfig; // Inyectar la configuración de JWT
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                request -> request.requestMatchers(WHITE_LIST_URLS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenConfig),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtAuthenticationFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenConfig))
                .addFilter(new JwtValidationFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenConfig))
                .csrf(config -> config.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(authenticationEntryPoint()));

        return http.build();
    }

    private static final String[] WHITE_LIST_URLS = {
            "/swagger-ui/**",
            "/api-docs/**",
    };

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> bodyResponse = new HashMap<>();
            bodyResponse.put("code", ApiCodes.API_CODE_401);
            bodyResponse.put("message", "Restricted access. Please log in to continue.");
            bodyResponse.put("error", "Unauthorized");
            response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));
        };
    }
}
