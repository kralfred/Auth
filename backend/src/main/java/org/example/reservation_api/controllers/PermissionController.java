package org.example.reservation_api.controllers;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.PermissionDTOs.*;

import org.example.reservation_api.services.PermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@Slf4j
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    @PreAuthorize("@myCustomBouncer.can(#currentUserId, #targetNestedGroupId, 'CREATE:permission:*')")
    public ResponseEntity<Void> createPermission(
            @AuthenticationPrincipal UUID currentUserId,
            @RequestParam UUID targetNestedGroupId,
            @RequestParam UUID ownerUsersGroupId,
            @Valid @RequestBody CreatePermissionRequest request
    ) {

        // Controller Filter: Validate query param matches body context
        if (!targetNestedGroupId.equals(request.nestedGroupId())) {
            throw new IllegalArgumentException("Query parameter targetNestedGroupId does not match payload nestedGroupId.");
        }

        // Delegate execution with clean, pre-defaulted values
        permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}