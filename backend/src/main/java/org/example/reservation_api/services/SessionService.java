package org.example.reservation_api.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.Device;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.example.reservation_api.repositories.DeviceRepository;
import org.example.reservation_api.repositories.SessionRepository;
import org.example.reservation_api.repositories.TokenRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;
    private final UserRepository userRepository;

    @Transactional
    public SessionResult createSessionForDevice(UUID userId, String deviceId) throws UnknownHostException {

        // 1. Extract HTTP Request metadata
        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String dpopJkt = request.getHeader("DPoP-JKT");

        Device device = deviceRepository.findById(deviceId).orElseGet(() -> {
            Device newDevice = new Device();
            newDevice.setId(deviceId);
            // Use JPA reference proxy instead of raw UUID
            newDevice.setUser(userRepository.getReferenceById(userId));
            return newDevice;
        });
        device.setUserAgent(userAgent);
        device.setLastLoginAt(OffsetDateTime.now());
        device = deviceRepository.save(device);

        // 3. Deactivate previous active session for this specific device
        sessionRepository.findByUserIdAndDeviceIdAndIsActiveTrue(userId, deviceId)
                .ifPresent(oldSession -> {
                    oldSession.setIsActive(false);
                    sessionRepository.save(oldSession);
                    tokenRepository.revokeAllBySessionId(oldSession.getId()); //[cite: 2]
                });

        // 4. Create new short-lived Session linked to Device and User
        Session session = new Session();
        session.setUser(userRepository.getReferenceById(userId));
        session.setDevice(device); // Passing the Device entity
        session.setDpopJkt(dpopJkt); //[cite: 2]
        session.setIpAddress(InetAddress.getByName(ipAddress)); //[cite: 2]
        session.setUserAgent(userAgent); //[cite: 2]
        session.setIsActive(true); //[cite: 2]
        session = sessionRepository.save(session); //[cite: 2]

        // 5. Issue and store the opaque refresh token
        String rawRefreshToken = jwtService.generateOpaqueRefreshToken(); //[cite: 2]
        RefreshToken refreshToken = new RefreshToken(); //[cite: 2]
        refreshToken.setSession(session); //[cite: 2]
        refreshToken.setTokenHash(SecurityUtils.hashToken(rawRefreshToken)); //[cite: 2]
        refreshToken.setExpiresAt(OffsetDateTime.now().plusDays(7)); //[cite: 2]
        refreshToken.setIsRevoked(false); //[cite: 2]
        refreshToken.setCreatedAt(OffsetDateTime.now()); //[cite: 2]
        tokenRepository.save(refreshToken); //[cite: 2]

        return new SessionResult(session.getId(), rawRefreshToken); //[cite: 2]
    }

    private String extractClientIp(HttpServletRequest request) { //[cite: 2]
        String ip = request.getHeader("X-Forwarded-For"); //[cite: 2]
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) { //[cite: 2]
            ip = request.getRemoteAddr(); //[cite: 2]
        } else {
            ip = ip.split(",")[0].trim(); //[cite: 2]
        }
        return ip; //[cite: 2]
    }

    public record SessionResult(UUID sessionId, String rawRefreshToken) {} //[cite: 2]
}