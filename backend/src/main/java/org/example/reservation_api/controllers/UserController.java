package org.example.reservation_api.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, UserService> {

    @Autowired
    public UserController(UserService service) {
        super(service);
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


    @PostMapping("/{id}/modify")
    @PreAuthorize("hasAuthority('can_modify_users')")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User user) {
        return ResponseEntity.ok(service.update(id, user));
    }




    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateSelf(Principal principal, @RequestBody User entity) {
        // principal.getName() gives you the username from the JWT
        return ResponseEntity.ok(service.selfUpdateUser(principal.getName(), entity));
    }
}
