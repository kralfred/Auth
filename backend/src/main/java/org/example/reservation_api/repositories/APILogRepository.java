package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.ApiLog;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class APILogRepository {

    private final JdbcClient jdbcClient;

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
}
