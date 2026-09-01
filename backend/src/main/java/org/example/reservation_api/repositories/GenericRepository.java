package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.Identifiable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.RecordComponent;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenericRepository {

    private final JdbcClient jdbcClient;
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public <T extends Identifiable> Optional<T> findById(String tableName, UUID id, Class<T> clazz) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(clazz)
                .optional();
    }

    public void saveMap(String tableName, Map<String, Object> parameters) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        MapSqlParameterSource paramSource = new MapSqlParameterSource();

        parameters.forEach((column, value) -> {
            columns.add("\"" + column + "\"");
            placeholders.add(":" + column);

            // Convert Instant to Timestamp if passed in map
            if (value instanceof java.time.Instant instantVal) {
                paramSource.addValue(column, java.sql.Timestamp.from(instantVal), java.sql.Types.TIMESTAMP);
            } else if (value instanceof UUID) {
                paramSource.addValue(column, value, java.sql.Types.OTHER);
            } else {
                paramSource.addValue(column, value);
            }
        });

        String sql = String.format("INSERT INTO \"%s\" (%s) VALUES (%s)", tableName, columns, placeholders);
        namedParameterJdbcTemplate.update(sql, paramSource);
    }
    public <T extends Identifiable> T save(String tableName, T entity) {
        Map<String, Object> params = convertRecordToMap(entity);
        List<String> columnList = new ArrayList<>(params.keySet());

        String columns = columnList.stream()
                .map(col -> "\"" + col + "\"")
                .collect(Collectors.joining(", "));

        String placeholders = columnList.stream()
                .map(col -> ":" + col)
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO \"%s\" (%s) VALUES (%s)", tableName, columns, placeholders);

        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        columnList.forEach(key -> {
            Object value = params.get(key);

            if (value instanceof java.time.Instant instantVal) {
                // Automatically convert Instant to Timestamp for database persistence
                paramSource.addValue(key, java.sql.Timestamp.from(instantVal), java.sql.Types.TIMESTAMP);
            } else if (value instanceof UUID) {
                paramSource.addValue(key, value, java.sql.Types.OTHER);
            } else {
                paramSource.addValue(key, value);
            }
        });

        log.info("Executing SQL: {}", sql);

        jdbcClient.sql(sql)
                .paramSource(paramSource)
                .update();

        log.info("Successfully inserted record into {}", tableName);
        return entity;
    }
    public <T extends Identifiable> List<T> findAll(Class<T> entityType) {
        String tableName = Identifiable.getTableName(entityType);
        String sql = String.format("SELECT * FROM \"%s\"", tableName);

        return jdbcClient.sql(sql)
                .query(entityType)
                .list();
    }
    public void logDatabaseDetails() {
        try (Connection conn = dataSource.getConnection()) {
            log.info("Connected Database Product: {}", conn.getMetaData().getDatabaseProductName());
            log.info("Connected Database URL: {}", conn.getMetaData().getURL());
            log.info("Connected Database User: {}", conn.getMetaData().getUserName());
            log.info("Current Active Schema: {}", conn.getSchema());
        } catch (Exception e) {
            log.error("Failed to fetch database connection metadata", e);
        }
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
        // MUST use LinkedHashMap to preserve exact field declaration order
        Map<String, Object> map = new LinkedHashMap<>();
        if (!record.getClass().isRecord()) {
            throw new IllegalArgumentException("Entity must be a Java record");
        }

        for (RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                Object value = component.getAccessor().invoke(record);
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
