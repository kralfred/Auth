package org.example.reservation_api.DTO;

import java.util.UUID;

public record ConfigureRuleRequest(
        UUID permissionId,
        UUID attributeId,
        boolean isRequired,
        String autoFillValue
) {}
