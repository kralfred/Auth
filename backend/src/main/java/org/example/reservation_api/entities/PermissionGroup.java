package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "permission_group")
public class PermissionGroup extends BaseEntity {
    private String name;

    @ManyToOne
    @JoinColumn(name = "nested_group_id")
    private NestedUserGroup nestedGroup;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "permission_group_id")
    private Set<PermissionRule> rules;

    @ManyToMany
    @JoinTable(
            name = "permission_group_members",
            joinColumns = @JoinColumn(name = "permission_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members;
}
