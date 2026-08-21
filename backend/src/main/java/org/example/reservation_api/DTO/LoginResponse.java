package org.example.reservation_api.DTO;

import java.util.List;
import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        String refreshToken,
        UUID userId,
        String username,
        List<String> permissions
) {
    // Canonical constructor overload for default token type
    public LoginResponse(String accessToken, long expiresIn, String refreshToken, UUID userId, String username, List<String> permissions) {
        this(accessToken, "Bearer", expiresIn, refreshToken, userId, username, permissions);
    }
}
