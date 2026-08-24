package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.GroupInvite;
import org.example.reservation_api.entities.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupInviteRepository {

    private final JdbcClient jdbcClient;

    public Optional<UUID> findByCodeAndActiveTrue(String code) {
        String sql = """
        SELECT u.nested_group_id FROM group_invite_target u
        JOIN group_invite o ON u.invite_id = o.id
        WHERE o.code = ?
          AND u.is_primary = true
        LIMIT 1
        """;
        return jdbcClient.sql(sql)
                .param("code", code)
                .query(UUID.class)
                .optional();
    }
}
