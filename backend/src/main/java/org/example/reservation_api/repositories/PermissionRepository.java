package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.PermissionInfo;
import org.example.reservation_api.entities.PermissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<PermissionRule, UUID> {

    @Query("SELECT COUNT(pg) > 0 FROM PermissionGroup pg " +
            "JOIN pg.members m " +
            "JOIN pg.rules r " +
            "WHERE m.id = :userId " +
            "AND pg.nestedGroup.id = :groupId " +
            "AND r.permissionName = :action")
    boolean hasPermissionInGroup(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId,
            @Param("action") String action);

    @Query("SELECT COUNT(pg) > 0 FROM PermissionGroup pg " +
            "JOIN pg.members m " +
            "JOIN pg.rules r " +
            "WHERE m.id = :userId " +
            "AND pg.nestedGroup.id = :groupId " +
            "AND r.permissionName = :action")
    boolean checkGroupPermission(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId,
            @Param("action") String action);
    // If you specifically need to check for a "Role" string like 'Group Admin'
    default boolean checkRolePermission(UUID userId, UUID groupId, String roleName) {
        return hasPermissionInGroup(userId, groupId, roleName);
    }
    default boolean hasRoleInGroup(UUID userId, UUID groupId, String roleName) {
        return hasPermissionInGroup(userId, groupId, roleName);
    }
    @Query("""
        SELECT new org.example.reservation_api.DTO.PermissionInfo(
            teg.name, 
            r.permissionName
        )
        FROM PermissionGroup pg 
        JOIN pg.members m 
        JOIN pg.rules r 
        JOIN r.targetEntityGroup teg
        WHERE m.id = :userId
    """)
    List<PermissionInfo> findAllCategorizedPermissions(@Param("userId") UUID userId);
    @Query("""
        SELECT DISTINCT r.permissionName 
        FROM PermissionGroup pg 
        JOIN pg.members m 
        JOIN pg.rules r 
        WHERE m.id = :userId 
        AND pg.nestedGroup.id = :groupId
    """)
    List<String> findPermissionsByUserAndGroup(
            @Param("userId") UUID userId,
            @Param("groupId") UUID groupId
    );

    @Query("""
        SELECT COUNT(pg) > 0 
        FROM PermissionGroup pg
        JOIN pg.members actor 
        JOIN pg.rules rule
        JOIN rule.targetEntityGroup teg
        JOIN teg.targetedUsers target
        WHERE actor.id = :actorId
        AND pg.nestedGroup.id = :groupId
        AND rule.permissionName = :action
        AND target.id = :targetId
    """)
    boolean checkTargetedPermission(
            @Param("actorId") UUID actorId,
            @Param("groupId") UUID groupId,
            @Param("action") String action,
            @Param("targetId") UUID targetId
    );
}