package org.example.reservation_api.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.ConfigureRuleRequest;
import org.example.reservation_api.DTO.PermissionDTOs.*;
import org.example.reservation_api.security.SecurityUtils;
import org.example.reservation_api.services.PermissionAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionAdminController {

    private final PermissionAdminService adminService;

    @PostMapping("/attributes/create")
    public ResponseEntity<Map<String, Object>> createAttribute(@RequestBody CreateAttributeRequest request) {

        log.error("Failed to persist API log: {}");

        UUID attributeId = adminService.registerNewAttribute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Targetable attribute registered successfully",
                "attributeId", attributeId
        ));
    }

    @PutMapping("/rules")
    public ResponseEntity<Map<String, String>> configureRule(@RequestBody ConfigureRuleRequest request) {
        adminService.configureAttributeRules(request);

        return ResponseEntity.ok(Map.of("message", "Permission attribute rule updated successfully"));
    }
}