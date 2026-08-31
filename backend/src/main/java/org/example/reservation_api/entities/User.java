package org.example.reservation_api.entities;

import java.util.UUID;

public record User(UUID id, String username,UUID defaultGroupId, UUID currentEnvironment) implements Identifiable {

    // Custom constructor omitting the ID parameter
    public User(String username,UUID defaultGroupId) {
        this(UUID.randomUUID(), username, defaultGroupId, defaultGroupId);
    }
}