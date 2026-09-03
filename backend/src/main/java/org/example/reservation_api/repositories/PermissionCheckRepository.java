package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PermissionCheckRepository {

    private final JdbcClient jdbcClient;

    public PermissionCheckRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean hasPermission(UUID nestedGroupId, UUID userId, String permissionCode) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM group_member gm
                JOIN group_permission gp 
                  ON gm.nested_group_id = gp.nested_group_id 
                 AND gm.group_id = gp.owner_users_group
                JOIN permission p 
                  ON gp.permission_id = p.id
                WHERE gm.nested_group_id = :nestedGroupId
                  AND gm.user_id = :userId
                  AND p.name = :permissionCode
            )
        """;

        return jdbcClient.sql(sql)
                .param("nestedGroupId", nestedGroupId)
                .param("userId", userId)
                .param("permissionCode", permissionCode)
                .query(Boolean.class)
                .single();
    }
}