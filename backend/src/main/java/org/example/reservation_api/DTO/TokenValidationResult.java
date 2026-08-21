package org.example.reservation_api.DTO;

import io.jsonwebtoken.Claims;

import java.util.UUID;

public record TokenValidationResult(
        UUID tokenId,
        UUID userId,
        Claims claims,
        ValidationStatus status
) {
    public enum ValidationStatus {
        VALID, EXPIRED, INVALID
    }

    public boolean isValid() {
        return status == ValidationStatus.VALID;
    }
}
