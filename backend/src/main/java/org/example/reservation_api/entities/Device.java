package org.example.reservation_api.entities;

import java.time.Instant;
import java.util.UUID;

public record Device(
        String id,
        UUID userId,
        String userAgent,
        Instant lastLoginAt,
        Instant createdAt
) {
    // Constructor for creating or updating device logins
    public Device(String id, UUID userId, String userAgent) {
        this(id, userId, userAgent, Instant.now(), Instant.now());
    }
}