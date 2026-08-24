package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.PermissionInfo;
import org.example.reservation_api.entities.Permission;
import org.example.reservation_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PermissionRepository {

    private final JdbcClient jdbcClient;

    public List<String> findUserEntityAccess(UUID userId, UUID groupId) {
        String sql = "SELECT * FROM fn_get_entity_access(?, ?)";

        return jdbcClient.sql(sql)
                .param(userId)
                .param(groupId)
                .query(String.class)
                .list();
    }
}