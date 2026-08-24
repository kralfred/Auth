package org.example.reservation_api.entities;

import java.util.UUID;

public record PermissionAttribute(
        UUID permissionId,
        UUID targetableAttributeId
) {}