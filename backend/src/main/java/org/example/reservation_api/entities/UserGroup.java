package org.example.reservation_api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_group")
public class UserGroup extends BaseEntity {


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "nested_group_id", nullable = false)
    private NestedGroup nestedGroup;

    @Size(max = 100)
    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "group")
    private Set<GroupMember> groupMembers = new LinkedHashSet<>();


    @OneToMany(mappedBy = "ownerUsersGroup")
    private Set<GroupPermission> groupPermissions = new LinkedHashSet<>();


}