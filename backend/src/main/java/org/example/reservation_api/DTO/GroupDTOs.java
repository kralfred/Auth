package org.example.reservation_api.DTO;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public sealed interface GroupDTOs {
     record Group(
            UUID id,
            String name,
            String baseUrl,
            Integer maxMembers,
            Long accessTokenTime,
            Long refreshTokenTime
    ) implements GroupDTOs {
    }
    record CreateNestedGroupRequest(
            String name,
            UUID parentGroupId,
            List<UUID> initialMemberIds,
            Optional<UUID> owner
    ) implements GroupDTOs{}
    record CreateUserGroupRequest(
            UUID nestedGroupId,
            String name,
            List<UUID> initialMembersIds
    ){}
     record UpdateGroupConfigRequest(
            Integer maxMembers,
            String baseUrl,
            Long refreshTokenTime,
            Long accessTokenTime
    ) implements GroupDTOs{}
    record NestedGroupConfiguration(
            UUID nestedGroupId,
            UUID defaultUserGroup,
            Integer maxMembers,
            String baseUrl,
            Long refreshTokenTime,
            Long accessTokenTime
    ) implements GroupDTOs{
        public static NestedGroupConfiguration defaultsFor(UUID nestedGroupId) {
            return new NestedGroupConfiguration(
                    nestedGroupId, null, 1000, null, 604800L, 900L);
        }
    }
}
