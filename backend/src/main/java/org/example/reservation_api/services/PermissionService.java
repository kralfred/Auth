package org.example.reservation_api.services;

import jakarta.transaction.Transactional;
import org.example.reservation_api.DTO.CreatePermissionRequest;
import org.example.reservation_api.repositories.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public void createAndAssignPermission(
            UUID targetNestedGroupId,
            UUID ownerUsersGroupId,
            CreatePermissionRequest request) {

        UUID actionId = permissionRepository.findOrCreateAction(request.actionType());
        UUID entityTypeId = permissionRepository.findOrCreateEntityType(request.targetTable());
        UUID attributeId = permissionRepository.findOrCreateTargetableAttribute(entityTypeId, request.attribute());


        String permissionName = request.toPermissionName();
        UUID permissionId = permissionRepository.findOrCreatePermission(permissionName, actionId, attributeId);

        // 3. Link to group_permission
        permissionRepository.assignPermissionToGroup(ownerUsersGroupId, permissionId, targetNestedGroupId);
    }


    public boolean hasPermission(UUID userId, UUID nestedGroupId, String permissionName) {
        // 1. Owner short-circuit: Owners implicitly have all permissions
        if (permissionRepository.isGroupOwner(userId, nestedGroupId)) {
            return true;
        }

        // 2. Query explicit permission mappings
        return permissionRepository.hasGroupPermission(userId, nestedGroupId, permissionName);
    }
}
