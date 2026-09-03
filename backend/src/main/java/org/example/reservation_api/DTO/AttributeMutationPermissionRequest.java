package org.example.reservation_api.DTO;

import java.util.List;
import java.util.Map;

public record AttributeMutationPermissionRequest(
        String action,
        String entityTypeName,
        List<String> allowedAttributes,
        List<String> requiredAttributes,
        Map<String, String> autoFilledAttributes
) implements CreatePermissionRequest {}