package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PermissionCheckRepository {

    private final JdbcClient jdbcClient;

    public static final UUID ROOT_SYSTEM_GROUP_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
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
    public boolean hasSystemPermission(UUID userId, String permissionName) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM group_member gm
                JOIN permission_attribute pa ON pa.nested_group_id = gm.nested_group_id
                JOIN permission p ON p.id = pa.permission_id
                JOIN nested_group ng ON ng.id = gm.nested_group_id
                WHERE gm.user_id = :userId
                  AND p.name = :permissionName
                  AND (ng.id = :rootGroupId OR ng.parent_group_id IS NULL)
            );
        """;

        return Boolean.TRUE.equals(
                jdbcClient.sql(sql)
                        .param("userId", userId)
                        .param("permissionName", permissionName)
                        .param("rootGroupId", ROOT_SYSTEM_GROUP_ID)
                        .query(Boolean.class)
                        .single()
        );
    }
}