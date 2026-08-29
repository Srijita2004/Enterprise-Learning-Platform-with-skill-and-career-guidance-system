package com.skillspherenexus.certificationmanagementservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

/**
 * Validates cryptographic JWT Bearer tokens using HMAC-SHA256 (HS256).
 */
@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);
    public static final String SECRET_KEY = "SkillSphereNexusSecretKeyForJwtSigningMustBeAtLeast256BitsLong2026Enterprise";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
                log.warn("JWT signature mismatch");
                return false;
            }

            Map<String, Object> claims = parsePayload(parts[1]);
            Number exp = (Number) claims.get("exp");
            if (exp != null && Instant.now().getEpochSecond() > exp.longValue()) {
                log.warn("JWT token expired");
                return false;
            }

            return true;
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
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
            log.warn("Could not extract claims: {}", e.getMessage());
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
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }
}
