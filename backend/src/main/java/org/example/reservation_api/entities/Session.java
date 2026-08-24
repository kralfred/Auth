package org.example.reservation_api.entities;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

public record Session(
        UUID id,
        UUID userId,
        String deviceId,
        String dpopJkt,
        String ipAddress,
        String userAgent,
        boolean isActive,
        Instant createdAt
) implements Identifiable {
    // Constructor for new active session creation
    public Session(UUID userId, String deviceId, String dpopJkt, String ipAddress, String userAgent) {
        this(UUID.randomUUID(), userId, deviceId, dpopJkt, ipAddress, userAgent, true, Instant.now());
    }
}