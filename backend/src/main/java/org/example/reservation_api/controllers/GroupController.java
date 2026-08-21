package org.example.reservation_api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.RoleAssignmentRequest;
import org.example.reservation_api.entities.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class GroupController {



    @PostMapping("/assign-role")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID groupId,
            @RequestBody RoleAssignmentRequest request,
            Authentication authentication) {

        User actor = (User) authentication.getPrincipal();
        return ResponseEntity.noContent().build();
    }
}