package org.example.reservation_api.services;

import org.example.reservation_api.repositories.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    /**
     * Evaluates whether a user has permission to perform an action on a nested group.
     * Group owners automatically bypass explicit permission checks.
     */
    public boolean hasPermission(UUID userId, UUID nestedGroupId, String permissionName) {
        // 1. Owner short-circuit: Owners implicitly have all permissions
        if (permissionRepository.isGroupOwner(userId, nestedGroupId)) {
            return true;
        }

        // 2. Query explicit permission mappings
        return permissionRepository.hasGroupPermission(userId, nestedGroupId, permissionName);
    }
}
