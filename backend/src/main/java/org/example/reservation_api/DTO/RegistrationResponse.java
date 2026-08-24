package org.example.reservation_api.DTO;

import java.util.UUID;

public record RegistrationResponse(
        UUID userId,
        UUID defaultNestedGroupId
) {}
