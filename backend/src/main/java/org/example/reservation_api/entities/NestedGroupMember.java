package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "nested_group_members")
@Data
@EqualsAndHashCode(callSuper = true)
public class NestedGroupMember extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "nested_group_id", nullable = false)
    private NestedUserGroup nestedGroup;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private GroupRole role; // Links the user to their permissions/groups
}
