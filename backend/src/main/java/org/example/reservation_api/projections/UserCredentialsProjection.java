package org.example.reservation_api.projections;

import java.util.UUID;

public record UserCredentialsProjection(
        UUID userId,
        String username,
        String password,
        UUID currentEnvironment
) {}
