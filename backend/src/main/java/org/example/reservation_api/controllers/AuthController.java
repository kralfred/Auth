package org.example.reservation_api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.LoginRequest;
import org.example.reservation_api.DTO.LoginResponse;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.RegistrationResponse;
import org.example.reservation_api.security.AppSecurityProperties;
import org.example.reservation_api.security.MyCustomBouncer;
import org.example.reservation_api.services.JwtService;
import org.example.reservation_api.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.UnknownHostException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MyCustomBouncer bouncer;
    private final AppSecurityProperties props;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) throws UnknownHostException {
            return ResponseEntity.ok(bouncer.tryLogin(request));
        }


    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {

        RegistrationResponse result = userService.tryRegister(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("Generated header: " + authHeader);
        String token = authHeader.substring(7);
        System.out.println("Generated token without header: " + token);
        return ResponseEntity.ok("Generated token without header: ");
    }
}
