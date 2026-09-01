package org.example.reservation_api.security;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.LoginRequest;
import org.example.reservation_api.DTO.LoginResponse;
import org.example.reservation_api.projections.UserCredentialsProjection;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.SystemSettingRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.example.reservation_api.services.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyCustomBouncer {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;
    private final SessionService sessionService;
    private final DpopService dpopService; // Injected DPoP parsing/validation service
    private final SystemSettingRepository systemSettingRepository;
    private final UserService userService;

    public boolean can(UUID userId, UUID nestedGroupId, String permission) {
        if (userId == null || nestedGroupId == null) {
            return false;
        }
        return permissionService.hasPermission(userId, nestedGroupId, permission);
    }

    @Transactional
    public LoginResponse tryLogin(LoginRequest request, String dpopHeader) throws UnknownHostException {

        UserCredentialsProjection credentials = userRepository.findCredentialsByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), credentials.password())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        log.error("Correct credentials for " + request.username());
        String dpopJkt = null;
        if (dpopHeader != null && !dpopHeader.isBlank()) {
            dpopJkt = dpopService.verifyAndExtractJkt(dpopHeader, "POST", "/api/auth/login");
        }
        log.error("dpopJkt extracted " + dpopJkt);
        SessionService.SessionResult sessionResult = sessionService.createSessionForDevice(
                credentials.userId(),
                request.deviceId(),
                dpopJkt
        );
        log.error("Session created " + sessionResult.rawRefreshToken());
        UUID currentGroupId = CurrentEnvironmentContext.get();
        if (currentGroupId == null) {

            currentGroupId = userService.getDefaultEnvironment(credentials.userId());
            CurrentEnvironmentContext.set(currentGroupId);
        }
        List<String> pageAccess = permissionRepository.findUserEntityAccess(credentials.userId(), currentGroupId);
        long expiration = 1200;
        if (pageAccess == null || pageAccess.isEmpty()) {
            log.error("pageAccess is empty for user: " + credentials.userId());
        } else {
            log.error("pageAccess not empty, first access element: " + pageAccess.getFirst());
        }

        String accessToken = jwtService.generateAccessToken(
                request.username(),
                currentGroupId,
                pageAccess,
                dpopJkt
        );
        log.error("Token generated " + accessToken);
        return new LoginResponse(
                accessToken,
                expiration,
                sessionResult.rawRefreshToken(),
                credentials.userId(),
                credentials.username(),
                pageAccess
        );
    }
}