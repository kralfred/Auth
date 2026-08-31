package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.Identifiable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenericRepository {

    private final JdbcClient jdbcClient;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public <T extends Identifiable> Optional<T> findById(String tableName, UUID id, Class<T> clazz) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(clazz)
                .optional();
    }
    public <T extends Identifiable> T save(String tableName, T entity) {
        Map<String, Object> params = convertRecordToMap(entity);

        String columns = String.join(", ", params.keySet());
        String placeholders = params.keySet().stream()
                .map(col -> ":" + col)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO public.\"%s\" (%s) VALUES (%s)", tableName, columns, placeholders);

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        params.forEach((key, value) -> {
            if (value instanceof UUID) {
                paramSource.addValue(key, value, java.sql.Types.OTHER);
            } else {
                paramSource.addValue(key, value);
            }
        });

        jdbcClient.sql(sql)
                .paramSource(paramSource)
                .update();

        return entity;
    }
    public <T extends Identifiable> List<T> findAll(Class<T> entityType) {
        String tableName = Identifiable.getTableName(entityType);
        String sql = String.format("SELECT * FROM \"%s\"", tableName);

        return jdbcClient.sql(sql)
                .query(entityType)
                .list();
    }

    public void addGroupMembers(String tableName,
                           String parentColumn,
                           UUID parentId,
                           String memberColumn,
                           List<UUID> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return;
        }

        String sql = String.format(
                "INSERT INTO %s (%s, %s) VALUES (?, ?) ON CONFLICT DO NOTHING",
                tableName, parentColumn, memberColumn
        );

        // Map member IDs to Object arrays for batching
        List<Object[]> batchArgs = memberIds.stream()
                .map(memberId -> new Object[]{parentId, memberId})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private Map<String, Object> convertRecordToMap(Object record) {
        Map<String, Object> map = new HashMap<>();
        if (!record.getClass().isRecord()) {
            throw new IllegalArgumentException("Entity must be a Java record");
        }

        for (RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                Object value = component.getAccessor().invoke(record);
                // Converts camelCase record fields (e.g. userId) to snake_case column names (user_id)
                String columnName = camelToSnakeCase(component.getName());
                map.put(columnName, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to map record field: " + component.getName(), e);
            }
        }
        return map;
    }

    private String camelToSnakeCase(String str) {
        return str.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
