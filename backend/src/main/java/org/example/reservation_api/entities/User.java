package org.example.reservation_api.entities;

import java.util.UUID;

public record User(
        UUID id,
        String username,
        UUID defaultEnvironment,
        UUID currentEnvironment
) implements BaseEntity {

    // Custom constructor omitting the ID parameter
    public User(String username, UUID defaultEnvironment) {
        this(UUID.randomUUID(), username, defaultEnvironment, defaultEnvironment);
    }
}