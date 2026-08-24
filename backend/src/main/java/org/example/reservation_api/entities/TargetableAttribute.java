package org.example.reservation_api.entities;

import java.util.UUID;

public record TargetableAttribute(
        UUID id,
        UUID entityTypeId,
        String name
) implements Identifiable {}