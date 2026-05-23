package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;


@RequiredArgsConstructor
@Data
@Entity
@Table(name = "entity_group")
public class EntityGroup extends BaseEntity {
    private String name;

    @ManyToOne
    @JoinColumn(name = "nested_group_id")
    private NestedUserGroup nestedGroup;

    @ManyToMany
    @JoinTable(
            name = "entity_group_members",
            joinColumns = @JoinColumn(name = "entity_group_id"),
            inverseJoinColumns = @JoinColumn(name = "entity_id") // Targeted users
    )
    private Set<User> targetedUsers;
}
