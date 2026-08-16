package org.example.reservation_api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class ReservationApiApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Allows you to run raw SQL directly

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:postgresql://localhost:5433/reservation_test_db?currentSchema=public");
        registry.add("spring.datasource.username", () -> "test_user");
        registry.add("spring.datasource.password", () -> "test_password");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }

    @Test
    void seedDatabaseForStressTest() {
        System.out.println("Starting database seeding on port 5433...");

        // Seed 100,000 users using the native Postgres generator we talked about!
        jdbcTemplate.execute("""
            INSERT INTO app_user (id, username, email, password)
            SELECT 
                gen_random_uuid(),
                'stress_user_' || i,
                'stress_' || i || '@test.com',
                '$2a$10$O0S1a7x...' 
            FROM generate_series(1, 100000) s(i)
            ON CONFLICT DO NOTHING;
        """);

        System.out.println("Seeding complete! Your SSD-safe test database is ready.");
    }
}