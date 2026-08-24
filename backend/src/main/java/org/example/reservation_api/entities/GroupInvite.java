package org.example.reservation_api.entities;

import java.util.UUID;

public record GroupInvite(
        UUID id,
        String code,
        boolean isActive
) implements Identifiable {}
