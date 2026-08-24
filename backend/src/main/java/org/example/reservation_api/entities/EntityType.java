package org.example.reservation_api.entities;

import java.util.UUID;

public record EntityType(
        UUID id,
        String name
) implements Identifiable {}