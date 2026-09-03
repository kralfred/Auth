package org.example.reservation_api.services;

import jakarta.transaction.Transactional;
import org.example.reservation_api.DTO.CreatePermissionRequest;
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




    public boolean hasPermission(UUID userId, UUID nestedGroupId, String permissionName) {
        if (permissionRepository.isGroupOwner(userId, nestedGroupId)) {
            return true;
        }

        return permissionRepository.hasGroupPermission(userId, nestedGroupId, permissionName);
    }
}
