package org.example.reservation_api.repositories;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.projections.UserCredentialsProjection;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
public class UserRepository extends AbstractJdbcRepository<User>{

    private final JdbcClient jdbcClient;

    public UserRepository(JdbcClient jdbcClient) {
        super(jdbcClient, User.class, "user");
        this.jdbcClient = jdbcClient;
    }

    public boolean checkExistingEmail(String email) {
        try {
            String sql = "SELECT COUNT(*) FROM user_info WHERE email = ?";
            Integer count = jdbcClient.sql(sql)
                    .param(email)
                    .query(Integer.class)
                    .single();
            return count != null && count > 0;
        } catch (Exception e) {
            log.error("Error executing checkExistingEmail for email: {}", email, e);
            throw e;
        }
    }

    public Optional<UUID> checkUsersDefaultEnvironment(UUID userId) {
        return jdbcClient.sql("SELECT default_group_id FROM \"user\" WHERE id = :userId")
                .param("userId", userId)
                .query(UUID.class)
                .optional();
    }


    public Optional<UserCredentialsProjection> findCredentialsByUsername(String username) {
        String sql = """
            SELECT 
                u.id AS user_id, 
                u.username, 
                ui.password, 
                u.current_environment 
            FROM "user" u
            JOIN user_info ui ON u.id = ui.user_id
            WHERE u.username = ?
            """;

        return jdbcClient.sql(sql)
                .param(username)
                .query(UserCredentialsProjection.class)
                .optional();
    }


}

