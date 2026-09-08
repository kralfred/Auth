package org.example.reservation_api.services;

import org.example.reservation_api.messages.AccessDeniedException;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final NestedGroupRepository nestedGroupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GenericRepository genericRepository;
    private final GroupRepository groupRepository;
    private final PermissionCheckRepository permissionCheckRepository;

    @Transactional
    public UUID createNewNestedGroup(String name, UUID parentGroup, UUID ownerId, List<UUID> groupMembers){

        UUID newNestedGroupId = UUID.randomUUID();
        UUID userGroupId = UUID.randomUUID();
        UserGroup nestedGroupMembers = new UserGroup(userGroupId, newNestedGroupId, "Nested Group Members");
        if(!groupMembers.contains(ownerId)){
            groupMembers.add(ownerId);
        }

        groupRepository.addUsersToGroup(groupMembers, userGroupId);

        NestedGroup newGroup = new NestedGroup(newNestedGroupId, name, parentGroup, ownerId);
        genericRepository.save("nested_group", newGroup);
        genericRepository.save("user_group", nestedGroupMembers);
       return newNestedGroupId;
    }
    @Transactional
    public UUID createNestedGroup(String groupName, UUID parentGroupId, UUID owner, List<UUID> initialMemberIds) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // 1. Permission Check: If creating under a parent group, verify user has permission in that scope
        if (parentGroupId != null) {
            boolean canCreateSubgroup = permissionCheckRepository.hasPermission(
                    parentGroupId,
                    currentUserId,
                    "CREATE_SUBGROUP"
            );
            if (!canCreateSubgroup) {
                throw new AccessDeniedException("Access denied: Cannot create subgroup under group " + parentGroupId);
            }
        }

        // 2. Resolve default values & attribute filtering
        UUID ownerId = Objects.requireNonNullElse(owner, currentUserId);

        List<UUID> members = (initialMemberIds != null && !initialMemberIds.isEmpty())
                ? new ArrayList<>(initialMemberIds)
                : new ArrayList<>();

        if (!members.contains(ownerId)) {
            members.add(ownerId);
        }

        UUID newGroupId = UUID.randomUUID();

        // 3. Database Operations
        groupRepository.createNestedGroup(newGroupId, groupName, parentGroupId, ownerId);
        groupRepository.addMembersToUserGroup(newGroupId, members);

        return newGroupId;
    }

    @Transactional
    public void createUserGroup(String groupName, UUID nestedGroupId, List<UUID> initialMemberIds){
        UUID newGroupId = UUID.randomUUID();
        groupRepository.createUserGroup(newGroupId, nestedGroupId, groupName);

        if(initialMemberIds != null){
            addUserToUserGroup(initialMemberIds, newGroupId);
        }
    }


    @Transactional
    public void addUserToUserGroup(List<UUID> targetUserId, UUID targetGroupId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Validate admin rights over the target group
        if (!permissionCheckRepository.hasPermission(targetGroupId, currentUserId, "MANAGE_GROUP_MEMBERS")) {
            throw new AccessDeniedException("Access denied: Cannot add members to group " + targetGroupId);
        }

        groupRepository.addMembersToUserGroup(targetGroupId, targetUserId);
    }
}
