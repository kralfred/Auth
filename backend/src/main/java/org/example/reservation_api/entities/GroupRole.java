package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.Set;

@Entity
@Table(name = "group_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "name"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class GroupRole extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // The boundary: this role only exists inside this specific group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private NestedUserGroup nestedGroup;

    // The "Packaged" Powers:
    // When a user is assigned this role, they get all permissions in these groups.
    @ManyToMany
    @JoinTable(
            name = "group_role_permission_groups",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_group_id")
    )
    private Set<PermissionGroup> permissionGroups;

    // List of users who currently hold this role within the group
    @OneToMany(mappedBy = "role")
    private Set<NestedGroupMember> members;
}
