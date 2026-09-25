package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.ConfigureRuleRequest;
import org.example.reservation_api.DTO.PermissionDTOs.*;
import org.example.reservation_api.entities.EntityType;
import org.example.reservation_api.messages.AccessDeniedException;
import org.example.reservation_api.repositories.PermissionAdminRepository;
import org.example.reservation_api.repositories.PermissionCheckRepository;
import org.example.reservation_api.security.CurrentEnvironmentContext;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionAdminService {

    private final PermissionAdminRepository adminRepository;
    private final PermissionCheckRepository permissionCheckRepository;


    private void enforceGlobalAdminAccess() {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        boolean isSystemAdmin = permissionCheckRepository.hasSystemPermission(
                currentUserId,
                "MANAGE_SYSTEM_PERMISSIONS"
        );

        if (!isSystemAdmin) {
            throw new AccessDeniedException("Access denied: Global permission administration privileges required.");
        }
    }

    @Transactional
    public List<EntityType> getAllEntities() {
        enforceGlobalAdminAccess(); // Global check added
        return adminRepository.listAllEntities();
    }

    @Transactional
    public String createEntity(String name) {
        enforceGlobalAdminAccess(); // Global check added
        UUID newEntityId = adminRepository.createEntity(name);
        return "Successfully created with ID: " + newEntityId;
    }

    @Transactional
    public UUID registerNewAttribute(CreateAttributeRequest request) {
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        log.error("Failed to persist API log: {}" + currentUserId);
        System.out.println("DEBUG Active User ID: " + currentUserId);
        boolean isSystemAdmin = permissionCheckRepository.hasSystemPermission(
                currentUserId,
                "MANAGE_SYSTEM_PERMISSIONS"
        );

        if (!isSystemAdmin) {
            throw new AccessDeniedException("Access denied: Global permission administration privileges required.");
        }

        return adminRepository.createAttribute(request.entityTypeName(), request.attributeName());
    }

    @Transactional
    public void configureAttributeRules(ConfigureRuleRequest request) {
        UUID activeGroupId = CurrentEnvironmentContext.get();
        UUID currentUserId = SecurityUtils.getCurrentUserId();

        boolean isAdmin = permissionCheckRepository.hasPermission(
                activeGroupId,
                currentUserId,
                "MANAGE_SYSTEM_PERMISSIONS"
        );

        if (!isAdmin) {
            throw new AccessDeniedException("Access denied: Permission Administration privileges required.");
        }

        adminRepository.configurePermissionAttributeRule(
                activeGroupId,
                request.permissionId(),
                request.attributeId(),
                request.isRequired(),
                request.autoFillValue()
        );
    }
}
