package org.example.reservation_api.entities;

import java.util.UUID;

public record CustomInfo(
        UUID id,
        String name,  // Nullable
        String value  // Nullable
) implements Identifiable {}