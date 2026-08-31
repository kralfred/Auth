package org.example.reservation_api.repositories;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SystemSettingRepository {

    private final JdbcClient jdbcClient;

    public SystemSettingRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Cacheable("systemSettings")
    public Optional<String> getValueByKey(String key) {
        String sql = "SELECT value FROM \"system_setting\" WHERE key = :key";
        return jdbcClient.sql(sql)
                .param("key", key)
                .query(String.class)
                .optional();
    }

    /**
     * Retrieve a configuration value parsed directly as a UUID.
     */
    public UUID getUuidValueByKey(String key) {
        return getValueByKey(key)
                .map(UUID::fromString)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing required system configuration key: " + key
                ));
    }
}
