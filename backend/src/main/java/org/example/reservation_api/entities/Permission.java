package org.example.reservation_api.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "permission")
public class Permission extends BaseEntity {


    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @OneToMany(mappedBy = "permission")
    private Set<GroupPermission> groupPermissions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "permission")
    private Set<PermissionAttribute> permissionAttributes = new LinkedHashSet<>();

}