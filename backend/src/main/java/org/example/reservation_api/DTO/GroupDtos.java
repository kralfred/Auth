package org.example.reservation_api.DTO;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public sealed interface GroupDtos {
    record CreateNestedGroupRequest(
            String name,
            UUID parentGroupId,
            UUID owner,
            List<UUID> initialMemberIds
    ) implements GroupDtos {}
    record CreateUserGroupRequest(
            String name,
            UUID nestedGroupId,
            List<UUID> initialMemberIds
    ) implements GroupDtos {}
}
