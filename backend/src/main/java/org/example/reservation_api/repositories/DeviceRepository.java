package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.Device;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
@RequiredArgsConstructor
public class DeviceRepository {

    private final JdbcClient jdbcClient;

    public void upsert(Device device) {
        String sql = """
            INSERT INTO device (id, user_id, user_agent, last_login_at, created_at)
            VALUES (:id, :userId, :userAgent, :lastLoginAt, :createdAt)
            ON CONFLICT (id) DO UPDATE SET
                user_id = EXCLUDED.user_id,
                user_agent = EXCLUDED.user_agent,
                last_login_at = EXCLUDED.last_login_at;
            """;

        jdbcClient.sql(sql)
                .param("id", device.id())
                .param("userId", device.userId())
                .param("userAgent", device.userAgent())
                .param("lastLoginAt", device.lastLoginAt() != null ? Timestamp.from(device.lastLoginAt()) : null)
                .param("createdAt", device.createdAt() != null ? Timestamp.from(device.createdAt()) : null)
                .update();
    }
}
