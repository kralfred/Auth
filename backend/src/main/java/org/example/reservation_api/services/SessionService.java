package org.example.reservation_api.services;

import jakarta.servlet.http.HttpServletRequest;
import org.example.reservation_api.entities.Device;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.example.reservation_api.repositories.DeviceRepository;
import org.example.reservation_api.repositories.GenericRepository;
import org.example.reservation_api.repositories.SessionRepository;
import org.example.reservation_api.repositories.TokenRepository;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class SessionService {

    private final DeviceRepository deviceRepository;
    private final SessionRepository sessionRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final HttpServletRequest request;
    private final GenericRepository genericRepository;

    public SessionService(DeviceRepository deviceRepository,
                          SessionRepository sessionRepository,
                          TokenRepository tokenRepository,
                          JwtService jwtService,
                          HttpServletRequest request, GenericRepository genericRepository) {
        this.deviceRepository = deviceRepository;
        this.sessionRepository = sessionRepository;
        this.tokenRepository = tokenRepository;
        this.jwtService = jwtService;
        this.request = request;
        this.genericRepository = genericRepository;
    }

    @Transactional
    public SessionResult createSessionForDevice(UUID userId, String deviceId, String dpopJkt) {

        // 1. Extract HTTP metadata
        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader("User-Agent");


        // 2. Upsert Device (No JPA proxies needed, just pure IDs)
        Device device = new Device(deviceId, userId, userAgent, Instant.now(), Instant.now());
        deviceRepository.upsert(device);

        // 3. Deactivate previous active session for this device
        sessionRepository.findByUserIdAndDeviceIdAndIsActiveTrue(userId, deviceId)
                .ifPresent(oldSession -> {
                    sessionRepository.deactivate(oldSession.id());
                    tokenRepository.revokeAllBySessionId(oldSession.id());
                });

        // 4. Create new Session via simple record constructor
        Session session = new Session(userId, deviceId, dpopJkt, ipAddress, userAgent);
        sessionRepository.saveSession(session);

        // 5. Issue and store opaque refresh token
        String rawRefreshToken = jwtService.generateOpaqueRefreshToken();
        String hashedToken = SecurityUtils.hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(session.id(), hashedToken);
        genericRepository.save("refresh_token",refreshToken);

        return new SessionResult(session.id(), rawRefreshToken);
    }

    private String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public record SessionResult(UUID sessionId, String rawRefreshToken) {}
}