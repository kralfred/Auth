package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PermissionRepository {

    private final JdbcClient jdbcClient;

    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * 1. Resolves or creates an action_id (e.g., "DELETE")
     */
    public UUID findOrCreateAction(String actionName) {
        String selectSql = "SELECT id FROM public.action WHERE name = :name";
        Optional<UUID> existing = jdbcClient.sql(selectSql)
                .paramSource(new MapSqlParameterSource().addValue("name", actionName, Types.VARCHAR))
                .query(UUID.class)
                .optional();

        if (existing.isPresent()) return existing.get();

        UUID id = UUID.randomUUID();
        jdbcClient.sql("INSERT INTO public.action (id, name) VALUES (:id, :name)")
                .paramSource(new MapSqlParameterSource()
                        .addValue("id", id, Types.OTHER)
                        .addValue("name", actionName, Types.VARCHAR))
                .update();
        return id;
    }

    /**
     * 2. Resolves or creates an entity_type_id (e.g., "user_logs")
     */
    public UUID findOrCreateEntityType(String entityName) {
        String selectSql = "SELECT id FROM public.entity_type WHERE name = :name";
        Optional<UUID> existing = jdbcClient.sql(selectSql)
                .paramSource(new MapSqlParameterSource().addValue("name", entityName, Types.VARCHAR))
                .query(UUID.class)
                .optional();

        if (existing.isPresent()) return existing.get();

        UUID id = UUID.randomUUID();
        jdbcClient.sql("INSERT INTO public.entity_type (id, name) VALUES (:id, :name)")
                .paramSource(new MapSqlParameterSource()
                        .addValue("id", id, Types.OTHER)
                        .addValue("name", entityName, Types.VARCHAR))
                .update();
        return id;
    }

    /**
     * 3. Resolves or creates a targetable_attribute_id for a given entity_type_id
     */
    public UUID findOrCreateTargetableAttribute(UUID entityTypeId, String attributeName) {
        String selectSql = "SELECT id FROM public.targetable_attribute WHERE entity_type_id = :entityTypeId AND name = :name";
        Optional<UUID> existing = jdbcClient.sql(selectSql)
                .paramSource(new MapSqlParameterSource()
                        .addValue("entityTypeId", entityTypeId, Types.OTHER)
                        .addValue("name", attributeName, Types.VARCHAR))
                .query(UUID.class)
                .optional();

        if (existing.isPresent()) return existing.get();

        UUID id = UUID.randomUUID();
        jdbcClient.sql("INSERT INTO public.targetable_attribute (id, entity_type_id, name) VALUES (:id, :entityTypeId, :name)")
                .paramSource(new MapSqlParameterSource()
                        .addValue("id", id, Types.OTHER)
                        .addValue("entityTypeId", entityTypeId, Types.OTHER)
                        .addValue("name", attributeName, Types.VARCHAR))
                .update();
        return id;
    }

    /**
     * 4. Resolves or creates the permission record and links it to targetable_attribute
     */
    public UUID findOrCreatePermission(String permissionName, UUID actionId, UUID targetableAttributeId) {
        String selectSql = "SELECT id FROM public.permission WHERE name = :name";
        Optional<UUID> existing = jdbcClient.sql(selectSql)
                .paramSource(new MapSqlParameterSource().addValue("name", permissionName, Types.VARCHAR))
                .query(UUID.class)
                .optional();

        if (existing.isPresent()) return existing.get();

        // Save permission
        UUID permissionId = UUID.randomUUID();
        jdbcClient.sql("INSERT INTO public.permission (id, name, action_id) VALUES (:id, :name, :actionId)")
                .paramSource(new MapSqlParameterSource()
                        .addValue("id", permissionId, Types.OTHER)
                        .addValue("name", permissionName, Types.VARCHAR)
                        .addValue("actionId", actionId, Types.OTHER))
                .update();

        // Link in permission_attribute
        jdbcClient.sql("INSERT INTO public.permission_attribute (permission_id, targetable_attribute_id) VALUES (:permId, :attrId)")
                .paramSource(new MapSqlParameterSource()
                        .addValue("permId", permissionId, Types.OTHER)
                        .addValue("attrId", targetableAttributeId, Types.OTHER))
                .update();

        return permissionId;
    }

    /**
     * 5. Assigns permission to group_permission
     */
    public void assignPermissionToGroup(UUID ownerUsersGroupId, UUID permissionId, UUID targetUsersGroupId) {
        String sql = """
            INSERT INTO public.group_permission (id, permission_id, owner_users_group, target_users_group)
            VALUES (:id, :permissionId, :ownerGroup, :targetGroup)
            """;

        jdbcClient.sql(sql)
                .paramSource(new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID(), Types.OTHER)
                        .addValue("permissionId", permissionId, Types.OTHER)
                        .addValue("ownerGroup", ownerUsersGroupId, Types.OTHER)
                        .addValue("targetGroup", targetUsersGroupId, Types.OTHER))
                .update();
    }
    public List<String> findUserEntityAccess(UUID userId, UUID groupId) {
        String sql = "SELECT * FROM fn_get_entity_access(?, ?)";

        return jdbcClient.sql(sql)
                .param(userId)
                .param(groupId)
                .query(String.class)
                .list();
    }
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
}