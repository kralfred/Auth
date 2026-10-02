package org.example.reservation_api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.ConfigureRuleRequest;
import org.example.reservation_api.DTO.PermissionDTOs.*;
import org.example.reservation_api.entities.EntityType;
import org.example.reservation_api.services.PermissionAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("@myCustomBouncer.canSystem('MANAGE_SYSTEM_PERMISSIONS')")
public class PermissionAdminController {

    private final PermissionAdminService adminService;


    @PutMapping("/rules")
    public ResponseEntity<Map<String, String>> configureRule(@RequestBody ConfigureRuleRequest request) {
        adminService.configureAttributeRules(request);

        return ResponseEntity.ok(Map.of("message", "Permission attribute rule updated successfully"));
    }
    @PostMapping("/entity/create")
    public ResponseEntity<CreateEntityResponse> createEntity( @Valid @RequestBody CreateEntityRequest request){
                String name = request.name();

                return ResponseEntity.ok(adminService.createEntity(name));
    }


    @GetMapping("/view/entities/all")
    public ResponseEntity<List<EntityType>> listAllEntities(
            @AuthenticationPrincipal UUID currentUserId
    ) {
        log.info("Fetching entities for user: {}", currentUserId);
        return ResponseEntity.ok(adminService.getAllEntities());
    }
    @GetMapping("/view/entities/{entityId}/attributes")
    public ResponseEntity<List<EntityAttribute>> getAttributesByEntity(@PathVariable UUID entityId) {
        List<EntityAttribute> attributes = adminService.getAttributesOfEntity(entityId);
        return ResponseEntity.ok(attributes);
    }
}