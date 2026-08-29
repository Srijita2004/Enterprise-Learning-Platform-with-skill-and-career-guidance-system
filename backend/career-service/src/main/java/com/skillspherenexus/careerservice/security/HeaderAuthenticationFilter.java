package com.skillspherenexus.careerservice.security;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Standard JWT Authentication Filter for Career Guidance Service (M4).
 * Validates cryptographic JWT Bearer tokens from the Authorization header,
 * with fallback to X-User-Role / X-User-Id headers.
 */
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ROLE_HEADER = "X-User-Role";
    public static final String USER_ID_HEADER = "X-User-Id";

    private static final Set<String> ALLOWED_ROLES = Set.of("ADMIN", "HR", "LEARNER", "EMPLOYEE");

    private final JwtTokenValidator jwtTokenValidator;

    public HeaderAuthenticationFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);
        boolean authenticated = false;

        // 1. Validate cryptographic JWT Bearer token
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (jwtTokenValidator.validateToken(token)) {
                String role = jwtTokenValidator.getRole(token);
                String userId = jwtTokenValidator.getUserId(token);

                if (role != null && !role.isBlank()) {
                    String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
                    String principal = (userId != null && !userId.isBlank()) ? userId : "authenticated-user";

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    authenticated = true;
                }
            }
        }

        // 2. Fallback to header-based identity if JWT not supplied
        if (!authenticated) {
            String role = request.getHeader(ROLE_HEADER);
            String userId = request.getHeader(USER_ID_HEADER);

            if (role != null && !role.isBlank()) {
                String normalizedRole = role.trim().toUpperCase(Locale.ROOT);

                if (ALLOWED_ROLES.contains(normalizedRole)) {
                    String principal = (userId != null && !userId.isBlank()) ? userId : "anonymous";

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + normalizedRole))
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
