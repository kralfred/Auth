package org.example.reservation_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import java.sql.SQLException;

public final class SecurityUtils {

    private SecurityUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hashes a raw opaque token string using SHA-256 for secure database storage.
     *
     * @param rawToken The plain text refresh token
     * @return The hex-encoded SHA-256 hash string (64 characters)
     */
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
