package org.example.reservation_api.entities;

import jakarta.persistence.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public record ApiLog(
        UUID id,
        String eventType,
        String method,
        String path,
        int status,
        long durationMs,
        UUID userId,
        Timestamp createdAt
) implements Identifiable {
   public ApiLog(String eventType, String method, String path, int status, long durationMs, UUID userId, Timestamp createdAt){
        this(UUID.randomUUID(),eventType,method, path, status, durationMs, userId, createdAt);
    }
}