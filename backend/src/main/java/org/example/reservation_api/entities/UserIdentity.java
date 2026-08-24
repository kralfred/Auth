package org.example.reservation_api.entities;


import java.util.UUID;

public record UserIdentity(
        UUID userId,
        String provider,
        String providerId // Nullable depending on strategy
) implements Identifiable {
    @Override
    public UUID id() {
        return userId;
    }
}