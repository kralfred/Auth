package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PermissionRepository {

    private final JdbcClient jdbcClient;

    public PermissionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<String> getPermissionTargetAttributes(UUID permissionId){
        String sql = "SELECT ta.name FROM targetable_attribute ta JOIN permission_attribute pa ON ta.id = pa.permission_id " +
                "WHERE pa = :permissionId";
        return jdbcClient.sql(sql)
                .param(permissionId)
                .query(String.class)
                .list();
    }



    public void createPermission(UUID permissionId, String permissionName, UUID actionId){
        String insertPermissionSql = """
        INSERT INTO permission (id, name, action_id, is_composite)
        VALUES (:id, :name, :actionId, false)
    """;

        jdbcClient.sql(insertPermissionSql)
                .param("id", permissionId)
                .param("name", permissionName)
                .param("actionId", actionId)
                .update();
    }
    public void addAttributeToPermission(UUID nestedGroupId, UUID permissionId, List<UUID> attributeIds, Boolean isRequired, String autoFillValue){
     String sql = "INSERT INTO permission_attribute (nested_group_id, permission_id, targetable_attribute_id, is_required, auto_fill_value)" +
             "VALUES (:nestedGroupId, :permissionId, :attributeId, :isRequired, :autoFillValue)\n" +
             "            ON CONFLICT DO NOTHING;";

     var batch = attributeIds.stream().map(attributeId -> Map.of("nestedGroupId", nestedGroupId,
             "permissionId", permissionId,
             "attributeId", attributeId,
             "isRequired", isRequired != null ? isRequired : false,
             "autoFillValue", autoFillValue != null ? autoFillValue : "")).toArray(Map[]::new);
     jdbcClient.sql(sql)
             .paramSource(batch)
             .update();
    }
    public boolean areAttributesAllowedForGroup(UUID nestedGroupId, List<UUID> targetableAttributeIds) {
        if (targetableAttributeIds == null || targetableAttributeIds.isEmpty()) {
            return true; // Nothing to validate
        }

        String sql = """
            SELECT COUNT(DISTINCT targetable_attribute_id)
            FROM permission_attribute
            WHERE nested_group_id = :nestedGroupId
              AND targetable_attribute_id = ANY(:attributeIds)
        """;

        UUID[] attributeArray = targetableAttributeIds.toArray(UUID[]::new);

        Integer matchingCount = jdbcClient.sql(sql)
                .param("nestedGroupId", nestedGroupId)
                .param("attributeIds", attributeArray)
                .query(Integer.class)
                .single();

        // If the distinct count in DB matches the input list size, all attributes exist for this group
        return matchingCount != null && matchingCount == targetableAttributeIds.stream().distinct().count();
    }




    public void assignPermissionToUserGroup(
            UUID nestedGroupId,
            UUID permissionId,
            UUID targetableAttributeId,
            Boolean isRequired,
            String autoFillValue) {

        String sql = """
            INSERT INTO permission_attribute (
                nested_group_id, 
                permission_id, 
                targetable_attribute_id, 
                is_required, 
                auto_fill_value
            )
            VALUES (:nestedGroupId, :permissionId, :targetableAttributeId, :isRequired, :autoFillValue)
        """;

        jdbcClient.sql(sql)
                .param("nestedGroupId", nestedGroupId)
                .param("permissionId", permissionId)
                .param("targetableAttributeId", targetableAttributeId)
                .param("isRequired", isRequired != null ? isRequired : false)
                .param("autoFillValue", autoFillValue)
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