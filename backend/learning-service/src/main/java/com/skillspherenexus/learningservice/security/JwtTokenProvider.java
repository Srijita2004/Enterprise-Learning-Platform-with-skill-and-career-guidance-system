package com.skillspherenexus.learningservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/**
 * Standard RFC 7519 JSON Web Token (JWT) provider using HMAC-SHA256 (HS256).
 * Signs and validates cryptographic tokens containing user claims.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    public static final String SECRET_KEY = "SkillSphereNexusSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026Enterprise";
    public static final long TOKEN_VALIDITY_SECONDS = 86400L; // 24 hours
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public String generateToken(UUID userId, String email, String fullName, String role) {
        try {
            long nowSeconds = Instant.now().getEpochSecond();
            long expSeconds = nowSeconds + TOKEN_VALIDITY_SECONDS;

            Map<String, Object> header = new LinkedHashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", userId.toString());
            payload.put("email", email);
            payload.put("name", fullName);
            payload.put("role", role);
            payload.put("roles", Collections.singletonList("ROLE_" + role.toUpperCase(Locale.ROOT)));
            payload.put("iat", nowSeconds);
            payload.put("exp", expSeconds);

            String encodedHeader = base64UrlEncode(OBJECT_MAPPER.writeValueAsString(header).getBytes(StandardCharsets.UTF_8));
            String encodedPayload = base64UrlEncode(OBJECT_MAPPER.writeValueAsString(payload).getBytes(StandardCharsets.UTF_8));
            String dataToSign = encodedHeader + "." + encodedPayload;

            String signature = signHmacSha256(dataToSign, SECRET_KEY);
            return dataToSign + "." + signature;
        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Could not generate authentication token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            if (token == null || token.isBlank()) {
                return false;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }

            String dataToSign = parts[0] + "." + parts[1];
            String expectedSignature = signHmacSha256(dataToSign, SECRET_KEY);

            if (!MessageDigest.isEqual(parts[2].getBytes(StandardCharsets.UTF_8), expectedSignature.getBytes(StandardCharsets.UTF_8))) {
                log.warn("JWT signature verification failed");
                return false;
            }

            Map<String, Object> claims = parsePayload(parts[1]);
            Number exp = (Number) claims.get("exp");
            if (exp != null && Instant.now().getEpochSecond() > exp.longValue()) {
                log.warn("JWT token has expired");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("JWT token validation exception: {}", e.getMessage());
            return false;
        }
    }

    public String getUserId(String token) {
        Map<String, Object> claims = getClaims(token);
        return claims != null ? (String) claims.get("sub") : null;
    }

    public String getRole(String token) {
        Map<String, Object> claims = getClaims(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public String getEmail(String token) {
        Map<String, Object> claims = getClaims(token);
        return claims != null ? (String) claims.get("email") : null;
    }

    public Map<String, Object> getClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                return parsePayload(parts[1]);
            }
        } catch (Exception e) {
            log.warn("Could not extract claims from token: {}", e.getMessage());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayload(String encodedPayload) throws Exception {
        byte[] bytes = Base64.getUrlDecoder().decode(encodedPayload);
        return OBJECT_MAPPER.readValue(bytes, Map.class);
    }

    private static String signHmacSha256(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(hmacBytes);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
