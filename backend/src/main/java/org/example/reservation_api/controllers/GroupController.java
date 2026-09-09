package org.example.reservation_api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.GroupDtos.*;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.services.GroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private GroupService groupService;

    @PostMapping("/assign-role")
    public ResponseEntity<Void> assignRole(
            @PathVariable UUID groupId,
            Authentication authentication) {

        User actor = (User) authentication.getPrincipal();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/create-nested")
    public ResponseEntity<UUID> createNestedGroup(@RequestBody @Valid CreateNestedGroupRequest request) {
        String name = (request.name() == null) ? "Default" : request.name();
        UUID newGroupId = groupService.createNestedGroup(
                name,
                request.parentGroupId(),
                request.owner(),
                request.initialMemberIds()
        );

        return ResponseEntity
                .created(URI.create("/api/groups/" + newGroupId))
                .body(newGroupId);
    }

    @PostMapping("create")
    public ResponseEntity<String> createUserGroup(@RequestBody @Valid CreateUserGroupRequest request){
        String name = (request.name() == null) ? "Default" : request.name();
        groupService.createUserGroup(name, request.nestedGroupId(), request.initialMemberIds());
        return ResponseEntity
                .created(URI.create("/api/groups/"))
                .body(name);
    }
}