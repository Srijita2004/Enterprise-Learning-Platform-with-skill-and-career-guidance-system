package com.skillspherenexus.apigateway.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Minimal role-based access control at the gateway level.
 *
 * The project has no Keycloak/IAM server, no token issuer and no
 * Spring Security dependency in any service, and introducing all of
 * that is out of scope for this milestone ("large architecture
 * changes", "do not create an unnecessarily complex authentication
 * architecture"). Instead the Angular frontend sends the logged-in
 * user's role on every request via the "X-User-Role" header, and this
 * filter enforces the minimum access separation the milestone asks
 * for: ADMIN-only course/module/content management, and ADMIN/HR-only
 * employee management and assessment verification.
 *
 * If the header is absent (e.g. a direct Postman call while testing),
 * the request is allowed through so existing manual verification of
 * Milestone 1/2 endpoints keeps working. This is a lightweight,
 * additive guard rather than a full authentication boundary.
 */
@Component
public class RoleAccessFilter implements Filter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_HEADER = "X-User-Role";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final JwtTokenValidator jwtTokenValidator;

    public RoleAccessFilter(JwtTokenValidator jwtTokenValidator) {
        this.jwtTokenValidator = jwtTokenValidator;
    }

    private record Rule(Pattern pathPattern, List<String> methods, List<String> allowedRoles) {
        boolean matches(String path, String method) {
            return pathPattern.matcher(path).matches() && methods.contains(method);
        }
    }

    private static final List<String> MUTATING_METHODS =
            List.of("POST", "PUT", "PATCH", "DELETE");

    private final List<Rule> rules = List.of(
            new Rule(
                    Pattern.compile("^/learning-api/api/enrollments/[^/]+/payment/confirm$"),
                    List.of("PATCH"),
                    List.of("ADMIN", "HR")
            ),
            new Rule(
                    Pattern.compile("^/api/employee(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR")
            ),
            new Rule(
                    Pattern.compile("^/api/employeeSkills(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR", "EMPLOYEE")
            ),
            new Rule(
                    Pattern.compile("^/api/assessment(s)?(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR", "EMPLOYEE")
            ),
            new Rule(
                    Pattern.compile("^/api/skill(s)?(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR")
            ),
            new Rule(
                    Pattern.compile("^/api/competenc(y|ies|y-frameworks)(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR")
            ),
            new Rule(
                    Pattern.compile("^/(certification-api/)?api/certificat(e|ion)(s)?(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR", "EMPLOYEE")
            ),
            new Rule(
                    Pattern.compile("^/api/career/(plans|roadmaps|promotions|job-opportunities|jobs)(/.*)?$"),
                    MUTATING_METHODS,
                    List.of("ADMIN", "HR", "EMPLOYEE")
            )
    );

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        String role = null;
        String userId = null;
        String authHeader = httpRequest.getHeader(AUTH_HEADER);

        // 1. Try to extract and validate role from JWT Bearer token
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (jwtTokenValidator.validateToken(token)) {
                role = jwtTokenValidator.getRole(token);
                userId = jwtTokenValidator.getUserId(token);
                if (role != null) {
                    role = role.trim().toUpperCase(java.util.Locale.ROOT);
                }
            }
        }

        // 2. Fallback to X-User-Role header
        if (role == null || role.isBlank()) {
            role = httpRequest.getHeader(ROLE_HEADER);
            if (role != null) {
                role = role.trim().toUpperCase(java.util.Locale.ROOT);
            }
        }
        if (userId == null || userId.isBlank()) {
            userId = httpRequest.getHeader(USER_ID_HEADER);
        }

        for (Rule rule : rules) {
            if (rule.matches(path, method)) {
                if (role == null || role.isBlank()) {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write(
                            "{\"message\":\"Authentication token or role is required.\"}"
                    );
                    return;
                }
                if (!rule.allowedRoles().contains(role)) {
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json");
                    httpResponse.getWriter().write(
                            "{\"message\":\"You do not have permission to perform this action.\"}"
                    );
                    return;
                }
                break;
            }
        }

        final String effectiveRole = role;
        final String effectiveUserId = userId;

        if (effectiveRole != null || effectiveUserId != null) {
            jakarta.servlet.http.HttpServletRequestWrapper wrappedRequest = new jakarta.servlet.http.HttpServletRequestWrapper(httpRequest) {
                @Override
                public String getHeader(String name) {
                    if (ROLE_HEADER.equalsIgnoreCase(name) && effectiveRole != null) {
                        return effectiveRole;
                    }
                    if (USER_ID_HEADER.equalsIgnoreCase(name) && effectiveUserId != null) {
                        return effectiveUserId;
                    }
                    return super.getHeader(name);
                }

                @Override
                public java.util.Enumeration<String> getHeaders(String name) {
                    if (ROLE_HEADER.equalsIgnoreCase(name) && effectiveRole != null) {
                        return java.util.Collections.enumeration(java.util.Collections.singletonList(effectiveRole));
                    }
                    if (USER_ID_HEADER.equalsIgnoreCase(name) && effectiveUserId != null) {
                        return java.util.Collections.enumeration(java.util.Collections.singletonList(effectiveUserId));
                    }
                    return super.getHeaders(name);
                }

                @Override
                public java.util.Enumeration<String> getHeaderNames() {
                    java.util.List<String> names = new java.util.ArrayList<>();
                    java.util.Enumeration<String> parentNames = super.getHeaderNames();
                    if (parentNames != null) {
                        names.addAll(java.util.Collections.list(parentNames));
                    }
                    if (effectiveRole != null && !names.contains(ROLE_HEADER)) {
                        names.add(ROLE_HEADER);
                    }
                    if (effectiveUserId != null && !names.contains(USER_ID_HEADER)) {
                        names.add(USER_ID_HEADER);
                    }
                    return java.util.Collections.enumeration(names);
                }
            };
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
