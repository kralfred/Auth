package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.repositories.PermissionRepository;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final PermissionRepository permissionRepository;

    /**
     * The core logic for your new architecture.
     * It checks if a path exists from User -> Role/Group -> Permission.
     */
    public boolean hasPermission(UUID userId, UUID nestedGroupId, String action) {
        // 1. Check if the user's scoped Role in this group has the permission
        boolean viaRole = permissionRepository.checkRolePermission(userId, nestedGroupId, action);

        // 2. Check if any functional Permission Groups the user is in have the permission
        boolean viaGroup = permissionRepository.checkGroupPermission(userId, nestedGroupId, action);

        return viaRole || viaGroup;
    }

    /**
     * For "Targeted" actions (e.g., Modify User B)
     */
    public boolean canPerformOnTarget(UUID userId, UUID nestedGroupId, String action, UUID targetId) {
        return permissionRepository.checkTargetedPermission(userId, nestedGroupId, action, targetId);
    }
    public void modifyUser(UUID actorId, UUID targetId, UUID groupId) throws AccessDeniedException {
        if (!permissionRepository.checkTargetedPermission(actorId, groupId, "MODIFY_USER", targetId)) {
            throw new AccessDeniedException("You do not have permission to modify this user.");
        }
    }
}
