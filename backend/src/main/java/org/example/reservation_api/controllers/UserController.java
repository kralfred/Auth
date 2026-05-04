package org.example.reservation_api.controllers;


import jakarta.validation.Valid;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.UserListResponse;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, UserService> {

    @Autowired
    public UserController(UserService service) {
        super(service);
    }

    @GetMapping("/accessible")
    public ResponseEntity<List<UserListResponse>> getAccessibleUsers(Authentication authentication) {
        // Get the logged-in user (Actor)
        User actor = (User) authentication.getPrincipal();

        assert actor != null;
        List<UserListResponse> users = service.getAccessibleUsersSummary(actor);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/swagger")
    public String redirectToSwagger() {
        return "redirect:/swagger-ui/index.html";
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("User Authorities: " + auth.getAuthorities());
        return super.getAll();
    }
    @PutMapping("/{id}/secure")
    public ResponseEntity<User> updateSecure(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> updates,
            Authentication authentication) {

        // 'actor' is the person performing the action
        User actor = (User) authentication.getPrincipal();

        User updatedUser = service.secureUpdate(actor, id, updates);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/modify")
    @PreAuthorize("hasAuthority('can_modify_users')")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(service.update(id, user));
    }

    @PutMapping("/{id}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> adminUpdate(@PathVariable UUID id, @RequestBody User entity) {
        return ResponseEntity.ok(service.adminUpdateUser(id, entity));
    }
    // UserController.java
    @PutMapping("/{id}/target-permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTargetPermissions(
            @PathVariable UUID id,
            @RequestBody List<UUID> targetIds) {

        service.updateUserTargetRelationships(id, targetIds);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateSelf(Principal principal, @RequestBody User entity) {
        // principal.getName() gives you the username from the JWT
        return ResponseEntity.ok(service.selfUpdateUser(principal.getName(), entity));
    }
}
