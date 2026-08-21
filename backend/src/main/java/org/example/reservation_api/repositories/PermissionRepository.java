package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.PermissionInfo;
import org.example.reservation_api.entities.Permission;
import org.example.reservation_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends BaseRepository<Permission> {

    @Query(value = "SELECT * FROM fn_get_entity_access(:userId, :groupId)", nativeQuery = true)
    List<String> findUserEntityAccess(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId
    );

}