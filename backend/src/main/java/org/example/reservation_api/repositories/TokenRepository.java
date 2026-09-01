package org.example.reservation_api.repositories;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class  TokenRepository  {

    private final JdbcClient jdbcClient;

    public void revokeAllBySessionId(UUID sessionId) {
        String sql = "UPDATE refresh_token SET is_revoked = true WHERE session_id = ?";

        jdbcClient.sql(sql)
                .param(sessionId) // Binds to the first ?
                .update();
    }

    public Optional<UUID> findByCodeAndActiveTrue(String code) {

        log.info("Starting token repo: {}", code);
        String sql = """
            SELECT u.nested_group_id FROM group_invite_target u
            JOIN group_invite o ON u.invite_id = o.id
            WHERE o.code = ?
              AND u.is_primary = true
            LIMIT 1
            """;

        try {
            return jdbcClient.sql(sql)
                    .param(code)
                    .query((rs, rowNum) -> rs.getObject("nested_group_id", UUID.class))
                    .optional();
        } catch (Exception e) {
            log.error("Error executing findTargetGroupIdByInviteCode for code: {}", code, e);
            throw e;
        }

    }
}


