package org.example.reservation_api.controllers;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.AuthDTOs.*;
import org.example.reservation_api.security.AppSecurityProperties;
import org.example.reservation_api.security.MyCustomBouncer;
import org.example.reservation_api.services.JwtService;
import org.example.reservation_api.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final MyCustomBouncer bouncer;
    private final AppSecurityProperties props;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            @RequestHeader(value = "DPoP", required = false) String dpopHeader,
            HttpServletResponse httpResponse
    ) throws UnknownHostException {

        LoginResponse response = bouncer.tryLogin(loginRequest, dpopHeader);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/api/auth")
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .sameSite("Strict")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {

        RegistrationResponse result = userService.tryRegister(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing or invalid 'Authorization: Bearer <token>' header");
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            return ResponseEntity.badRequest().body("Token string is empty");
        }

        try {
            TokenValidationResult validationResult = bouncer.checkToken(token);

            if (validationResult.status() != TokenValidationResult.ValidationStatus.VALID) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Token validation failed: " + validationResult.status());
            }

            AuthResponse response = mapToAuthResponse(token, validationResult);
            return ResponseEntity.ok(response);

        } catch (MalformedJwtException e) {
            return ResponseEntity.badRequest().body("Malformed JWT token format");
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or untrusted token");
        }
    }

    private AuthResponse mapToAuthResponse(String token, TokenValidationResult result) {
        String email = result.claims() != null ? result.claims().get("email", String.class) : null;


        long expiresIn = 0;
        if (result.claims() != null && result.claims().getExpiration() != null) {
            Instant expiresAt = result.claims().getExpiration().toInstant();
            expiresIn = Math.max(0, Duration.between(Instant.now(), expiresAt).getSeconds());
        }

        UserDto user = new UserDto(
                result.userId(),
                result.username(),
                email,
                result.permissions() != null ? result.permissions() : List.of()
        );

        return new AuthResponse(
                user,
                token,
                "Bearer",
                expiresIn,
                null // No refresh token on validation
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Missing or invalid 'Authorization: Bearer <token>' header");
        }

        System.out.println("Generated header: " + authHeader);
        String token = authHeader.substring(7);
        bouncer.checkToken(token); // Executes checkToken, throwing an exception if invalid[cite: 3]

        return ResponseEntity.ok("Token is valid");
    }
}
