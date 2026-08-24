package org.example.reservation_api.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

public record ApiLog(
        UUID id,
        String eventType,
        String method,
        String path,
        int status,
        long durationMs,
        UUID userId, // Nullable (unauthenticated requests)
        Instant createdAt
) implements Identifiable {
   public ApiLog(String eventType, String method, String path, int status, long durationMs, UUID userId, Instant createdAt){
        this(UUID.randomUUID(),eventType,method, path, status, durationMs, userId, createdAt);
    }
}