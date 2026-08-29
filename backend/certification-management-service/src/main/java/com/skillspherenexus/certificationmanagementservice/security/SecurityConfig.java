package com.skillspherenexus.certificationmanagementservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Enables role-based access control (RBAC) across the Certification
 * Management Service (Milestone 3). Same stateless, header-driven mechanism
 * as M1 and M2 — see {@link HeaderAuthenticationFilter}.
 *
 * Every endpoint in this service is only ever called from the ADMIN/HR
 * "Certification Management" section of the frontend, so authorization is
 * enforced per-controller via {@code @PreAuthorize("hasAnyRole('ADMIN','HR')")}.
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
}
