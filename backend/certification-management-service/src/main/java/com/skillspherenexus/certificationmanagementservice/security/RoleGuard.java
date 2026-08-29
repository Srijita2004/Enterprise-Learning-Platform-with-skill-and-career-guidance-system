package com.skillspherenexus.certificationmanagementservice.security;

import com.skillspherenexus.certificationmanagementservice.exception.ForbiddenOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Locale;

public final class RoleGuard {
    private RoleGuard() {}

    public static RequestActor manager(String userId, String role) {
        String normalized = resolveRole(role);
        if (!normalized.equals("ADMIN") && !normalized.equals("HR") && !normalized.equals("TRAINING_MANAGER")) {
            throw new ForbiddenOperationException("Only Admin, HR or Training Manager can perform this Certification Management operation");
        }
        String actor = resolveUserId(userId);
        return new RequestActor(actor, normalized);
    }

    public static RequestActor actor(String userId, String role) {
        String normalized = resolveRole(role);
        if (normalized.isEmpty()) {
            normalized = "LEARNER";
        }
        String actor = resolveUserId(userId);
        return new RequestActor(actor, normalized);
    }

    private static String resolveRole(String role) {
        if (role != null && !role.isBlank()) {
            return role.trim().toUpperCase(Locale.ROOT);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority ga : auth.getAuthorities()) {
                String authority = ga.getAuthority();
                if (authority.startsWith("ROLE_")) {
                    return authority.substring(5).toUpperCase(Locale.ROOT);
                }
                return authority.toUpperCase(Locale.ROOT);
            }
        }
        return "";
    }

    private static String resolveUserId(String userId) {
        if (userId != null && !userId.isBlank()) {
            return userId.trim();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null) {
            return auth.getPrincipal().toString();
        }
        return "unknown";
    }
}

