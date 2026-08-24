package org.example.reservation_api.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final NestedGroupRepository nestedGroupRepository;
    private final GroupInviteRepository groupInviteRepository;
    private final GenericRepository genericRepository;
    private final GroupRepository groupRepository;

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
}
