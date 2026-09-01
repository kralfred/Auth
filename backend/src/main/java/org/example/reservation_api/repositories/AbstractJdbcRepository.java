package org.example.reservation_api.repositories;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.BaseEntity;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractJdbcRepository<T extends BaseEntity> implements BaseRepository<T> {

    protected final JdbcClient jdbcClient;
    protected final Class<T> entityClass;
    protected final String tableName;

    protected AbstractJdbcRepository(JdbcClient jdbcClient, Class<T> entityClass, String tableName) {
        this.jdbcClient = jdbcClient;
        this.entityClass = entityClass;
        // Escape table name to handle PostgreSQL reserved keywords
        this.tableName = "\"" + tableName + "\"";
    }

    @Override
    public List<T> dbFindAll() {
        String sql = String.format("SELECT * FROM %s", tableName);
        return jdbcClient.sql(sql)
                .query(entityClass)
                .list();
    }

    @Override
    public Optional<T> dbFindById(UUID id) {
        String sql = String.format("SELECT * FROM %s WHERE id = :id", tableName);
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(entityClass)
                .optional();
    }

    @Override
    public boolean existsById(UUID id) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE id = :id", tableName);
        Integer count = jdbcClient.sql(sql)
                .param("id", id)
                .query(Integer.class)
                .single();
        return count != null && count > 0;
    }

    @Override
    public T dbUpdate(T entity) {
        Map<String, Object> params = convertRecordToMap(entity);
        params.remove("id"); // Keep primary key out of SET clause

        String setClause = params.keySet().stream()
                .map(col -> "\"" + col + "\" = :" + col)
                .collect(Collectors.joining(", "));

        String sql = String.format("UPDATE %s SET %s WHERE id = :id", tableName, setClause);

        MapSqlParameterSource paramSource = buildParamSource(params);
        paramSource.addValue("id", entity.id(), java.sql.Types.OTHER);

        jdbcClient.sql(sql)
                .paramSource(paramSource)
                .update();

        return entity;
    }

    // Helper: Convert record fields (camelCase) to DB column names (snake_case)
    private Map<String, Object> convertRecordToMap(Object record) {
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

    private MapSqlParameterSource buildParamSource(Map<String, Object> params) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        params.forEach((key, value) -> {
            if (value instanceof Instant instantVal) {
                paramSource.addValue(key, java.sql.Timestamp.from(instantVal), java.sql.Types.TIMESTAMP);
            } else if (value instanceof UUID) {
                paramSource.addValue(key, value, java.sql.Types.OTHER);
            } else {
                paramSource.addValue(key, value);
            }
        });
        return paramSource;
    }

    private String camelToSnakeCase(String str) {
        return str.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
