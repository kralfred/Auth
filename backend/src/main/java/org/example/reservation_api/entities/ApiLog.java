package org.example.reservation_api.entities;

import java.sql.Timestamp;
import java.util.UUID;

public record ApiLog(
        UUID id,
        UUID nestedGroupId,
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
    public ApiLog(UUID nestedGroupId, String eventType, String method, String path, int status, long durationMs, UUID userId, Timestamp createdAt, String errorDetails) {
        this(UUID.randomUUID(),nestedGroupId , eventType, method, path, status, durationMs, userId, createdAt, errorDetails);
    }
}