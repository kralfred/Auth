package org.example.reservation_api.security;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.LoginRequest;
import org.example.reservation_api.DTO.LoginResponse;
import org.example.reservation_api.DTO.TokenValidationResult;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.example.reservation_api.security.CurrentEnvironmentContext;
import org.example.reservation_api.projections.UserCredentialsProjection;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.TokenRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.example.reservation_api.services.JwtService;
import org.example.reservation_api.services.PermissionService;
import org.example.reservation_api.services.SessionService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import java.net.UnknownHostException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyCustomBouncer {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final PermissionService permissionService;
    private final TokenRepository tokenRepository;
    private final SessionService sessionService;

    public boolean can(UUID userId, UUID nestedGroupId, String permission) {
        if (userId == null || nestedGroupId == null) {
            return false;
        }
        return permissionService.hasPermission(userId, nestedGroupId, permission);
    }

    @Transactional
    public LoginResponse tryLogin(LoginRequest request) throws UnknownHostException {
        // 1. Validate credentials
        UserCredentialsProjection credentials = userRepository.findCredentialsByUsername(request.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), credentials.passwordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        // 2. Delegate Session & Metadata creation to SessionService
        SessionService.SessionResult sessionResult = sessionService.createSessionForDevice(
                credentials.userId(),
                request.deviceId()
        );

        // 3. Resolve permissions for active environment
        List<String> views = permissionRepository.findUserEntityAccess(
                credentials.userId(),
                credentials.currentEnvironment()
        );
        UUID currentGroupId = CurrentEnvironmentContext.get();
        List<String> pageAccess = permissionRepository.findUserEntityAccess(credentials.userId(),currentGroupId);
        long expiration = 1200;


        String accessToken = jwtService.generateAccessToken(
                request.username(),
                currentGroupId,
                pageAccess
        );

        return new LoginResponse(accessToken, expiration,sessionResult.rawRefreshToken(),
                credentials.userId(), credentials.username(),pageAccess);
    }


}