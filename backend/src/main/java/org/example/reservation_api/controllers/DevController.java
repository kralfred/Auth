package org.example.reservation_api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.services.GroupService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.example.reservation_api.DTO.GroupDTOs.*;
@RestController
@RequestMapping("/api/testing")
@RequiredArgsConstructor
public class DevController {

    private final GroupService groupService;

    @PostMapping("/create-else")
    public UUID createGroup(@RequestBody CreateNestedGroupRequest request) {
        return groupService.createNestedGroup(request.name(), request.parentGroupId(),request.owner(),request.initialMemberIds());
    }


    @PostMapping("/update-else")
    public ResponseEntity<String> updateGroup(@RequestBody NestedGroupConfiguration request) {
        // Hardcode a test user — this endpoint is not for real users
        groupService.updateNestedGroupConfiguration(request);
        return ResponseEntity.ok("nice");
    }
}
