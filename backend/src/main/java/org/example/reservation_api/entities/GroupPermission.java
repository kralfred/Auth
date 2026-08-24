package org.example.reservation_api.entities;

import java.util.UUID;

public record GroupPermission(
        UUID id,
        UUID permissionId,
        UUID ownerUsersGroup,
        UUID targetUsersGroup
) implements Identifiable {}