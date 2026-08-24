package org.example.reservation_api.entities;

import java.util.UUID;

public record Action(
        UUID id,
        String name
) implements Identifiable {}