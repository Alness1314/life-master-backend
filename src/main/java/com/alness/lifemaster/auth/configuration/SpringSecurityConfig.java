package com.alness.lifemaster.auth.configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.alness.lifemaster.auth.filters.JwtAuthenticationFilter;
import com.alness.lifemaster.auth.filters.JwtValidationFilter;
import com.alness.lifemaster.auth.filters.UserOwnershipFilter;
import com.alness.lifemaster.auth.session.RevokedTokenService;
import com.alness.lifemaster.users.repository.UserRepository;
import com.alness.lifemaster.utils.ApiCodes;
import com.alness.lifemaster.operations.audit.AuditEventService;
import com.alness.lifemaster.operations.audit.AuditFilter;
import com.alness.lifemaster.permissions.security.PermissionAuthorizationManager;
import com.alness.lifemaster.swagger.SwaggerPaths;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {
    
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenConfig jwtTokenConfig;
    private final AuditEventService auditEventService;
    private final UserRepository userRepository;
    private final RevokedTokenService revokedTokenService;
    private final PermissionAuthorizationManager permissionAuthorizationManager;

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
        String apiPrefix = jwtTokenConfig.getApiPrefix();
        JwtAuthenticationFilter authenticationFilter = new JwtAuthenticationFilter(
                authenticationConfiguration.getAuthenticationManager(), jwtTokenConfig);
        http.authorizeHttpRequests(
                request -> request.requestMatchers("/", apiPrefix + "/auth").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(SwaggerPaths.securityMatchers()).permitAll()
                        .requestMatchers("/actuator/**").hasAuthority("Administrator")
                        .requestMatchers(apiPrefix + "/auth/**").authenticated()
                        .requestMatchers(apiPrefix + "/users/me/**", apiPrefix + "/catalogs/currencies")
                        .authenticated()
                        .requestMatchers(apiPrefix + "/users/*/files/**").authenticated()
                        .requestMatchers(apiPrefix + "/helper/**").hasAuthority("Administrator")
                        .requestMatchers(apiPrefix + "/**").access(permissionAuthorizationManager)
                        .anyRequest().authenticated())
                .addFilterAt(authenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilter(new JwtValidationFilter(authenticationConfiguration.getAuthenticationManager(), jwtTokenConfig,
                        userRepository, revokedTokenService))
                .addFilterAfter(new UserOwnershipFilter(), JwtValidationFilter.class)
                .addFilterAfter(new AuditFilter(auditEventService), UserOwnershipFilter.class)
                .csrf(config -> config.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

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

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            Map<String, Object> bodyResponse = new HashMap<>();
            bodyResponse.put("code", ApiCodes.API_CODE_403);
            bodyResponse.put("message", "No tienes permisos para realizar esta operación.");
            bodyResponse.put("error", "Forbidden");
            response.getWriter().write(new ObjectMapper().writeValueAsString(bodyResponse));
        };
    }
}
