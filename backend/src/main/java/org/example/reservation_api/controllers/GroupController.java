package org.example.reservation_api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.RoleAssignmentRequest;
import org.example.reservation_api.entities.EntityGroup;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.services.GroupService;
import org.example.reservation_api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;


    @PostMapping("/assign-role")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID groupId,
            @RequestBody RoleAssignmentRequest request,
            Authentication authentication) {

        User actor = (User) authentication.getPrincipal();
        // The controller just passes the 'context' to the service
        groupService.updateUserRoles(actor.getId(), groupId, request);
        return ResponseEntity.noContent().build();
    }
}