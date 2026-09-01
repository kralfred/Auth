package org.example.reservation_api.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record ApiLog(
        UUID id,
        String eventType,
        String method,
        String path,
        int status,
        long durationMs,
        UUID userId,
        Timestamp createdAt,
        String errorDetails
) implements Identifiable {

    // Compact or convenient secondary constructor
    public ApiLog(String eventType, String method, String path, int status, long durationMs, UUID userId, Timestamp createdAt, String errorDetails) {
        this(UUID.randomUUID(), eventType, method, path, status, durationMs, userId, createdAt, errorDetails);
    }
}