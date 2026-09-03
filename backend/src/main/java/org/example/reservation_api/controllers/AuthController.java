package org.example.reservation_api.controllers;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("Generated header: " + authHeader);
        String token = authHeader.substring(7);
        bouncer.checkToken(token);
        return ResponseEntity.ok("Generated token without header: ");
    }
}
