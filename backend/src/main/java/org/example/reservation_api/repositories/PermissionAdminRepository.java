package org.example.reservation_api.repositories;

import org.example.reservation_api.DTO.PermissionDTOs;
import org.example.reservation_api.entities.EntityType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.example.reservation_api.DTO.PermissionDTOs.EntityAttribute;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class PermissionAdminRepository {

    private final JdbcClient jdbcClient;

    public PermissionAdminRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<EntityAttribute> findAttributesOfEntity(UUID entityId){
        String sql = "SELECT * FROM targetable_attribute WHERE entity_type_id = :entityId";
        return jdbcClient.sql(sql)
                .param("entityId", entityId)
                .query(EntityAttribute.class)
                .list();
    }


    public int grantAttributesToGroup(UUID groupId, List<UUID> attributeIds) {
        if (attributeIds.isEmpty()) return 0;

        UUID[] ids = attributeIds.toArray(UUID[]::new);

        return jdbcClient.sql("""
            INSERT INTO "group_attribute_grant"
                ("nested_group_id", "targetable_attribute_id")
            SELECT :groupId, a
            FROM unnest(:ids::uuid[]) AS a
            ON CONFLICT DO NOTHING
            """)
                .param("groupId", groupId)
                .param("ids", ids)
                .update();
    }

    public UUID createAttribute(UUID EntityTypeId, String attributeName) {
        String sql = """
        INSERT INTO targetable_attribute (id, entity_type_id, name)
        VALUES (
            gen_random_uuid(),
            :EntityTypeId,
            :attributeName
        )
        ON CONFLICT (entity_type_id, name) DO UPDATE SET name = EXCLUDED.name
        RETURNING id;
    """;

        return jdbcClient.sql(sql)
                .param("EntityTypeId", EntityTypeId)
                .param("attributeName", attributeName)
                .query(UUID.class)
                .single();
    }

    public UUID createEntity(String entityTypeName){
        String sql = "INSERT INTO entity_type (id, name)" +
                "VALUES (gen_random_uuid(), :entityTypeName)" +
                "RETURNING id";

        return jdbcClient.sql(sql)
                .param("entityTypeName", entityTypeName)
                .query(UUID.class)
                .single();
    }

    public List<EntityType> listAllEntities(){
        String sql = "SELECT * FROM entity_type";


        return jdbcClient.sql(sql)
                .query(EntityType.class).list();
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
                permission_id, 
                targetable_attribute_id, 
                is_required, 
                auto_fill_value
            )
            VALUES (:nestedGroupId, :permissionId, :attributeId, :isRequired, :autoFillValue)
            ON CONFLICT (permission_id, targetable_attribute_id)
            DO UPDATE SET 
                is_required = EXCLUDED.is_required,
                auto_fill_value = EXCLUDED.auto_fill_value;
        """;

        jdbcClient.sql(sql)
                .param("permissionId", permissionId)
                .param("attributeId", attributeId)
                .param("isRequired", isRequired)
                .param("autoFillValue", autoFillValue)
                .update();
    }
}