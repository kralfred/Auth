package org.example.reservation_api.entities;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record Session(
        UUID id,
        UUID userId,
        String deviceId,
        String dpopJkt,
        String ipAddress,
        String userAgent,
        boolean isActive,
        OffsetDateTime createdAt
) implements Identifiable {
    public Session(UUID userId, String deviceId, String dpopJkt, String ipAddress, String userAgent) {
        this(
                UUID.randomUUID(),
                userId,
                deviceId,
                dpopJkt,
                ipAddress,
                userAgent,
                true,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }
}