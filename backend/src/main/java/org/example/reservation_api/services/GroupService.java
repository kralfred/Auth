package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.RoleAssignmentRequest;
import org.example.reservation_api.entities.NestedUserGroup;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.repositories.GroupRepository;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AccessControlService accessControlService;

    public void updateUserRoles(UUID actorId, UUID groupId, RoleAssignmentRequest request) {
        // Check specific action: "CAN_MANAGE_ROLES" within "groupId"
        if (!accessControlService.hasPermission(actorId, groupId, "CAN_MANAGE_ROLES")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient permissions in this group");
        }

        // Proceed with logic...
    }
    public void performGroupAdminAction(UUID actorId, UUID groupId) {
        // Check if the user is a 'Group Admin' for this specific nested group
        boolean isGroupAdmin = accessControlService.hasPermission(actorId, groupId, "Group Admin");

        if (!isGroupAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not an admin of this group");
        }

        // Proceed with logic...
    }
}
