package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, UUID> {

    // Optimized check: "Does this user have THIS permission in THIS nested group?"
    @Query("""
        SELECT COUNT(up) > 0 
        FROM User u 
        JOIN u.grantedPermissions up 
        WHERE u.id = :userId 
        AND up.nestedGroupId = :nestedGroupId 
        AND up.permissionName = 'can_view_logs'
    """)
    boolean hasLogViewCapability(@Param("userId") UUID userId,
                                 @Param("nestedGroupId") UUID nestedGroupId);
}
