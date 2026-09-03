package org.example.reservation_api.controllers;

import org.example.reservation_api.DTO.CreatePermissionRequest;
import org.example.reservation_api.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    /**
     * Endpoint to create a dynamic permission and attach it to a user group.
     * POST /api/v1/permissions?targetNestedGroupId=...&ownerUsersGroupId=...
     */
    @PostMapping
    @PreAuthorize("@myCustomBouncer.can(#currentUserId, #targetNestedGroupId, 'CREATE:permission:*')")
    public ResponseEntity<Void> createPermission(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam UUID targetNestedGroupId,
            @RequestParam UUID ownerUsersGroupId,
            @RequestBody CreatePermissionRequest request
    ) {

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}