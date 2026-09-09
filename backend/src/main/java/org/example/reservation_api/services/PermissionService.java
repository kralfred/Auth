package org.example.reservation_api.services;

import org.example.reservation_api.DTO.PermissionDTOs;
import org.springframework.transaction.annotation.Transactional;
import org.example.reservation_api.repositories.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    public List<String> getPermissionAttributes(UUID targetNestedGroupId, UUID targetPermissionId){
       return permissionRepository.getPermissionTargetAttributes(targetPermissionId);
    }

    @Transactional
    public void createPermission(PermissionDTOs.CreatePermissionRequest request) {
        // Service Rule: Validate attribute assignment against database
        if (!request.targetableAttributeIds().isEmpty()) {
            boolean allowed = permissionRepository.areAttributesAllowedForGroup(
                    request.nestedGroupId(),
                    request.targetableAttributeIds()
            );

            if (!allowed) {
                throw new IllegalArgumentException(
                        "One or more requested attributes are not permitted for group: " + request.nestedGroupId()
                );
            }
        }

        UUID newPermissionId = UUID.randomUUID();

        // 1. Create base permission record using defaulted name
        permissionRepository.createPermission(
                newPermissionId,
                request.permissionName(),
                request.actionId()
        );

        // 2. Only run batch attribute insert if targetable attributes were supplied
        if (!request.targetableAttributeIds().isEmpty()) {
            permissionRepository.addAttributeToPermission(
                    request.nestedGroupId(),
                    newPermissionId,
                    request.targetableAttributeIds(),
                    request.isRequired(),
                    request.autoFillValue()
            );
        }
    }

    public boolean hasPermission(UUID userId, UUID nestedGroupId, String permissionName) {
        if (permissionRepository.isGroupOwner(userId, nestedGroupId)) {
            return true;
        }

        return permissionRepository.hasGroupPermission(userId, nestedGroupId, permissionName);
    }
}
