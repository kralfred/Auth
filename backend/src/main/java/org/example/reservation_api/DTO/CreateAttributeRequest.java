package org.example.reservation_api.DTO;

import java.util.UUID;

public record CreateAttributeRequest(
        String entityTypeName,
        String attributeName
) {}


