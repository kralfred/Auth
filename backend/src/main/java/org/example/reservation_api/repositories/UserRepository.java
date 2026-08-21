package org.example.reservation_api.repositories;

import jakarta.transaction.Transactional;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.projections.GlobalCapabilityProjection;
import org.example.reservation_api.projections.UserCredentialsProjection;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends BaseRepository<User> {

    @Query(value = "SELECT fn_register_user(:username, :email, :name, :passwordHash)", nativeQuery = true)
    UUID registerUser(
            @Param("username") String username,
            @Param("email") String email,
            @Param("name") String name,
            @Param("passwordHash") String passwordHash
    );

    @Query(value = "SELECT * FROM fn_verify_user_credentials(:username)", nativeQuery = true)
    Optional<UserCredentialsProjection> findCredentialsByUsername(@Param("username") String username);

    @Modifying
    @Query("UPDATE User u SET u.currentEnvironment = :groupId WHERE u.id = :userId")
    void updateCurrentEnvironment(@Param("userId") UUID userId, @Param("groupId") UUID groupId);


    Optional<User> findByUsername(String username);

}

