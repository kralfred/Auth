package org.example.reservation_api.entities;

import java.util.UUID;

public record GroupMember(
        UUID userId,
        UUID groupId
) {}