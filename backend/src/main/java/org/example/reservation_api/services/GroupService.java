package org.example.reservation_api.services;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.GroupDTOs.*;
import org.example.reservation_api.messages.AccessDeniedException;
import org.example.reservation_api.security.CurrentEnvironmentContext;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final NestedGroupRepository nestedGroupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GenericRepository genericRepository;
    private final GroupRepository groupRepository;
    private final PermissionCheckRepository permissionCheckRepository;

    @Transactional
    public UUID createNestedGroup(String groupName, UUID parentGroupId,
                                  Optional<UUID> owner, List<UUID> initialMemberIds) {
        UUID ownerId = owner.orElse(SecurityUtils.getCurrentUserId());

        UUID newNestedGroupId   = UUID.randomUUID();
        UUID defaultUserGroupId = UUID.randomUUID();

        // 1. The tenant itself
        groupRepository.createNestedGroup(newNestedGroupId, groupName, parentGroupId, ownerId);

        // 2. Default user_group inside it
        groupRepository.createUserGroup(defaultUserGroupId, newNestedGroupId, "Default");

        // 3. Config row — this is the piece that was missing
        groupRepository.createDefaultConfiguration(newNestedGroupId, defaultUserGroupId);

        // 4. Members go into the default user_group, not the nested_group
        List<UUID> members = initialMemberIds != null
                ? new ArrayList<>(initialMemberIds)
                : new ArrayList<>();
        if (!members.contains(ownerId)) members.add(ownerId);

        groupRepository.addMembersToUserGroup(defaultUserGroupId, members);

        return newNestedGroupId;
    }

    @Transactional
    public void updateNestedGroupConfiguration(NestedGroupConfiguration updateRequest){
      groupRepository.updateConfiguration(CurrentEnvironmentContext.get(), updateRequest);
    }

    @Transactional
    public String getCurrentEnvironment(UUID currentGroupId){
       return groupRepository.findCurrentNestedGroupName(currentGroupId);
    }
    @Transactional
    public NestedGroupConfiguration getNestedGroupConfiguration(UUID targetGroupId) {
        return groupRepository.findNestedGroupConfiguration(targetGroupId)
                .orElseGet(() -> NestedGroupConfiguration.defaultsFor(targetGroupId));
    }

    public Optional<Group> getGroupInfo(UUID currentGroupId){
        return groupRepository.findById(currentGroupId);
    }

    @Transactional
    public void createUserGroup(String groupName, UUID nestedGroupId, List<UUID> initialMemberIds){
        UUID newGroupId = UUID.randomUUID();
        groupRepository.createUserGroup(newGroupId, nestedGroupId, groupName);

        if(initialMemberIds != null){
            addUserToUsersGroup(initialMemberIds, newGroupId);
        }
    }


    @Transactional
    public void addUserToUsersGroup(List<UUID> targetUserId, UUID targetGroupId) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        // Validate admin rights over the target group
        if (!permissionCheckRepository.hasPermission(targetGroupId, currentUserId, "MANAGE_GROUP_MEMBERS")) {
            throw new AccessDeniedException("Access denied: Cannot add members to group " + targetGroupId);
        }

        groupRepository.addMembersToUserGroup(targetGroupId, targetUserId);
    }
}
