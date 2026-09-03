package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.Session;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SessionRepository {

    private final JdbcClient jdbcClient;
    private final GenericRepository genericRepository;

    public Optional<Session> findByUserIdAndDeviceIdAndIsActiveTrue(UUID userId, String deviceId) {
        String sql = """
        SELECT * FROM session 
        WHERE user_id = :userId 
          AND device_id = :deviceId 
          AND is_active = true 
        LIMIT 1
        """;

        return jdbcClient.sql(sql)
                .param("userId", userId)
                .param("deviceId", deviceId)
                .query(Session.class)
                .optional();
    }


    public void saveSession(Session session) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", session.id());
        params.put("user_id", session.userId());
        params.put("device_id", session.deviceId());
        params.put("dpop_jkt", session.dpopJkt());
        params.put("ip_address", session.ipAddress());
        params.put("user_agent", session.userAgent());
        params.put("is_active", session.isActive());

        // Custom conversion specifically for Session timestamp
        params.put("created_at", session.createdAt() != null ? Timestamp.from(session.createdAt()) : null);

        genericRepository.saveMap("session", params);
    }

    public void deactivate(UUID sessionId) {
        String sql = "UPDATE session SET is_active = false WHERE id = ?";
        jdbcClient.sql(sql).param(sessionId).update();
    }

}
