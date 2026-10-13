package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public List<String> getListOfSystemPermissions(UUID userId, UUID systemNestedGroupId) {
        String sql = """
        WITH user_groups AS (
            SELECT ug.id
            FROM group_member gm
            JOIN user_group ug ON ug.id = gm.group_id
            WHERE gm.user_id         = ?
              AND gm.nested_group_id = ?
        )
        SELECT p.name
        FROM user_groups ug
        JOIN group_permission gp
          ON gp.owner_users_group = ug.id
         AND gp.nested_group_id   = ?
        JOIN permission p ON p.id = gp.permission_id
        ORDER BY p.name;
        """;

        return jdbcClient.sql(sql)
                .param(userId)
                .param(systemNestedGroupId)
                .param(systemNestedGroupId)
                .query(String.class)
                .list();
    }



    public boolean hasSystemPermissions(UUID userId, UUID systemGroupId){
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM group_member gm
                JOIN nested_group ng
                  ON ng.id = gm.group_id
                WHERE gm.user_id = :userId
                  AND ng.id     = :systemGroupId
            );
        """;
        return Boolean.TRUE.equals(
                jdbcClient.sql(sql)
                        .param("userId", userId)
                        .param("systemGroupId", systemGroupId)
                        .query(Boolean.class)
                        .single()
        );
    }

    public boolean hasSystemPermission(UUID userId, String permissionName) {
        String sql = """
            SELECT EXISTS (
                SELECT 1
                FROM group_member gm
                JOIN group_permission gp
                  ON gp.owner_users_group = gm.group_id
                 AND gp.nested_group_id   = gm.nested_group_id
                JOIN permission p ON p.id = gp.permission_id
                WHERE gm.user_id = :userId
                  AND p.name     = :permissionName
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

    public boolean isGroupOwner(UUID groupId, UUID userId) {
        return jdbcClient.sql("""
            SELECT EXISTS (
                SELECT 1 FROM nested_group
                WHERE id = ? AND owner = ?
            )
            """)
                .param(groupId)
                .param(userId)
                .query(Boolean.class)
                .single();
    }

    public boolean ownerCanTargetAttribute(UUID groupId, UUID userId, UUID attributeId) {
        return jdbcClient.sql("""
            SELECT EXISTS (
                SELECT 1
                FROM nested_group ng
                JOIN targetable_attribute ta ON ta.id = ?
                WHERE ng.id = ?
                  AND ng.owner = ?
                  AND (
                         EXISTS (
                            SELECT 1 FROM group_attribute_grant gag
                            WHERE gag.nested_group_id = ng.id
                              AND gag.targetable_attribute_id = ta.id
                        )
                  )
            )
            """)
                .param(attributeId)
                .param(groupId)
                .param(userId)
                .query(Boolean.class)
                .single();
    }
}