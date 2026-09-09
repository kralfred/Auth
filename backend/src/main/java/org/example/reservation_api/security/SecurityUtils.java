package org.example.reservation_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.SQLException;
import java.util.UUID;

public final class SecurityUtils {


    public record UserPrincipal(UUID id, String username) {}
    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user ID found in security context");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof UUID uuid) {
            return uuid;
        } else if (principal instanceof String strId) {
            try {
                return UUID.fromString(strId);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Principal string is not a valid UUID: " + strId);
            }
        }

        throw new IllegalStateException("No authenticated user ID found in security context");
    }
    public static String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PGobject toPgObject(Object object) {
        if (object == null) {
            return null;
        }
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(objectMapper.writeValueAsString(object));
            return pgObject;
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSONB", e);
        }
    }
}
