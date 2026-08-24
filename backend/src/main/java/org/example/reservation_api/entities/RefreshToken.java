package org.example.reservation_api.entities;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record RefreshToken(
        UUID id,
        UUID sessionId,
        String tokenHash,
        Instant expiresAt,
        boolean isRevoked,
        Instant createdAt
) implements Identifiable {
    // Constructor for creating a default 7-day token
    public RefreshToken(UUID sessionId, String tokenHash) {
        this(UUID.randomUUID(), sessionId, tokenHash, Instant.now().plus(7, ChronoUnit.DAYS), false, Instant.now());
    }
}