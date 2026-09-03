package org.example.reservation_api.services;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.ConfigureRuleRequest;
import org.example.reservation_api.DTO.CreateAttributeRequest;
import org.example.reservation_api.messages.AccessDeniedException;
import org.example.reservation_api.repositories.PermissionAdminRepository;
import org.example.reservation_api.repositories.PermissionCheckRepository;
import org.example.reservation_api.security.CurrentEnvironmentContext;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PermissionAdminService {

    private final PermissionAdminRepository adminRepository;
    private final PermissionCheckRepository permissionCheckRepository;


    @Transactional
    public UUID registerNewAttribute(CreateAttributeRequest request) {
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
