package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "nested_group")
public class NestedUserGroup extends BaseEntity {
    private String name;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private NestedUserGroup parent;

    @OneToMany(mappedBy = "parent")
    private List<NestedUserGroup> children;

    // References the membership join table
    @OneToMany(mappedBy = "nestedGroup")
    private Set<NestedGroupMember> memberships;

    // Permissions available to be assigned within this group
    @OneToMany(mappedBy = "nestedGroup")
    private Set<PermissionGroup> permissionGroups;
}
