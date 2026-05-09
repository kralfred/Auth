package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_permission")
public class UserPermission extends BaseEntity {

    private UUID nestedGroupId;

    @Column(nullable = false)
    private String permissionName;

    // The ID of the entity_group containing the affected users/entities
    private UUID groupId;

    // Optional: Reference back to users who have this permission
    @ManyToMany(mappedBy = "grantedPermissions")
    private Set<User> actors;

    public Object getTargetId() {
    }

    // Getters and Setters...
}
