package org.example.reservation_api.entities;

import java.util.UUID;

public record Permission(
        UUID id,
        String name,
        UUID actionId
) implements Identifiable {}