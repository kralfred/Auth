package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Repository
public class PermissionRepository {

    private final JdbcClient jdbcClient;

    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Checks if the given user is the owner of the specified nested group.
     */
    public boolean isGroupOwner(UUID userId, UUID nestedGroupId) {
        String sql = "SELECT COUNT(*) FROM public.nested_group WHERE id = :groupId AND owner = :userId";

        Long count = jdbcClient.sql(sql)
                .paramSource(new MapSqlParameterSource()
                        .addValue("groupId", nestedGroupId, Types.OTHER)
                        .addValue("userId", userId, Types.OTHER))
                .query(Long.class)
                .single();

        return count > 0;
    }

    /**
     * Checks if a user possesses a specific permission for a target nested group via group memberships.
     */
    public boolean hasGroupPermission(UUID userId, UUID nestedGroupId, String permissionName) {
        String sql = """
            SELECT COUNT(*)
            FROM public.group_member gm
            JOIN public.group_permission gp ON gm.group_id = gp.owner_users_group
            JOIN public.permission p ON gp.permission_id = p.id
            WHERE gm.user_id = :userId
              AND gp.target_users_group IN (
                  SELECT id FROM public.user_group WHERE nested_group_id = :groupId
              )
              AND p.name = :permissionName
            """;

        Long count = jdbcClient.sql(sql)
                .paramSource(new MapSqlParameterSource()
                        .addValue("userId", userId, Types.OTHER)
                        .addValue("groupId", nestedGroupId, Types.OTHER)
                        .addValue("permissionName", permissionName, Types.VARCHAR))
                .query(Long.class)
                .single();

        return count > 0;
    }
    public List<String> findUserEntityAccess(UUID userId, UUID groupId) {
        String sql = "SELECT * FROM fn_get_entity_access(?, ?)";

        return jdbcClient.sql(sql)
                .param(userId)
                .param(groupId)
                .query(String.class)
                .list();
    }
}