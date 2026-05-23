package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;


@Data
@Entity
@Table(name = "app_user")
@NoArgsConstructor // Required for JPA reflection
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // Optional: Bi-directional link to see what groups this user belongs to
    @OneToMany(mappedBy = "user")
    private Set<NestedGroupMember> groupMemberships;

    // Optional: Bi-directional link to functional permission groups
    @OneToMany(mappedBy = "user")
    private Set<NestedGroupMember>nestedGroupMemberships;
}
