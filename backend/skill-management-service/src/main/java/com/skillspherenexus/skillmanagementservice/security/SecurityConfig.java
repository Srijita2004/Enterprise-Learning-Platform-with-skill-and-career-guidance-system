package com.skillspherenexus.skillmanagementservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Enables role-based access control (RBAC) across every controller in the
 * Skill Management Service (Milestone 1).
 *
 * - Stateless: no server-side session, no CSRF token needed (this is a pure
 *   REST API consumed by the Angular SPA, not form-based browser auth).
 * - CORS is left to the existing {@code CorsConfig} bean; this config only
 *   makes sure security does not block the CORS preflight (OPTIONS) requests.
 * - Authorization decisions themselves live on the controllers via
 *   {@code @PreAuthorize}, which {@code @EnableMethodSecurity} activates.
 * - Any request without a recognized role header is treated as anonymous and
 *   rejected by the method-level checks (401/403), see
 *   {@link RestAuthenticationEntryPoint} and {@link RestAccessDeniedHandler}.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                     JwtTokenValidator jwtTokenValidator) throws Exception {
        HeaderAuthenticationFilter headerAuthenticationFilter = new HeaderAuthenticationFilter(jwtTokenValidator);
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator", "/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                        .accessDeniedHandler(new RestAccessDeniedHandler())
                )
                .addFilterBefore(headerAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
