package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.Session;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SessionRepository {

    private final JdbcClient jdbcClient;

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

    public void deactivate(UUID sessionId) {
        String sql = "UPDATE session SET is_active = false WHERE id = ?";
        jdbcClient.sql(sql).param(sessionId).update();
    }

}
