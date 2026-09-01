package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.ErrorDetails;
import org.example.reservation_api.entities.ApiLog;
import org.example.reservation_api.security.SecurityUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class APILogRepository {

    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    public List<ApiLog> findLogs(int page, int size, String eventType, Integer status) {
        int offset = page * size;

        String sql = """
            SELECT 
                id, 
                event_type AS eventType, 
                method, 
                path, 
                status, 
                duration_ms AS durationMs, 
                user_id AS userId, 
                created_at AS createdAt
            FROM api_log
            WHERE (:eventType IS NULL OR event_type = :eventType)
              AND (:status IS NULL OR status = :status)
            ORDER BY created_at DESC
            LIMIT :limit OFFSET :offset
            """;

        return jdbcClient.sql(sql)
                .param("eventType", eventType)
                .param("status", status)
                .param("limit", size)
                .param("offset", offset)
                .query(ApiLog.class)
                .list();
    }
    public void saveLog(ApiLog log) {
        String sql = """
            INSERT INTO "api_log" (
                id, 
                event_type, 
                method, 
                path, 
                status, 
                duration_ms, 
                user_id, 
                created_at, 
                error_details
            ) VALUES (
                :id, 
                :eventType, 
                :method, 
                :path, 
                :status, 
                :durationMs, 
                :userId, 
                :createdAt, 
                :errorDetails::jsonb
            )
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", log.id())
                .addValue("eventType", log.eventType())
                .addValue("method", log.method())
                .addValue("path", log.path())
                .addValue("status", log.status())
                .addValue("durationMs", log.durationMs())
                .addValue("userId", log.userId())
                .addValue("createdAt", log.createdAt())
                .addValue("errorDetails", log.errorDetails());

        // Call update on namedParameterJdbcTemplate
        namedParameterJdbcTemplate.update(sql, params);
    }
}
