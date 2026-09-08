package org.example.reservation_api.repositories;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PermissionAdminRepository {

    private final JdbcClient jdbcClient;

    public PermissionAdminRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public UUID createAttribute(String entityTypeName, String attributeName) {
        String sql = """
        INSERT INTO targetable_attribute (id, entity_type_id, name)
        VALUES (
            gen_random_uuid(),
            (SELECT id FROM entity_type WHERE name = :entityTypeName),
            :attributeName
        )
        ON CONFLICT (entity_type_id, name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id;
    """;

        return jdbcClient.sql(sql)
                .param("entityTypeName", entityTypeName)
                .param("attributeName", attributeName)
                .query(UUID.class)
                .single();
    }


    public void configurePermissionAttributeRule(
            UUID nestedGroupId,
            UUID permissionId,
            UUID attributeId,
            boolean isRequired,
            String autoFillValue
    ) {
        String sql = """
            INSERT INTO permission_attribute (
                nested_group_id, 
                permission_id, 
                targetable_attribute_id, 
                is_required, 
                auto_fill_value
            )
            VALUES (:nestedGroupId, :permissionId, :attributeId, :isRequired, :autoFillValue)
            ON CONFLICT (nested_group_id, permission_id, targetable_attribute_id)
            DO UPDATE SET 
                is_required = EXCLUDED.is_required,
                auto_fill_value = EXCLUDED.auto_fill_value;
        """;

        jdbcClient.sql(sql)
                .param("nestedGroupId", nestedGroupId)
                .param("permissionId", permissionId)
                .param("attributeId", attributeId)
                .param("isRequired", isRequired)
                .param("autoFillValue", autoFillValue)
                .update();
    }
}