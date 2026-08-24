package org.example.reservation_api.entities;

import java.util.UUID;

public record GroupInviteTarget(
        UUID inviteId,
        UUID nestedGroupId,
        boolean isPrimary
) {}
