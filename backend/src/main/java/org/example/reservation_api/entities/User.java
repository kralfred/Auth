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
@Table(name = "\"user\"")
public class User extends BaseEntity {


    @Size(max = 100)
    @NotNull
    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "current_environment", nullable = false)
    private NestedGroup currentEnvironment;

    @OneToMany(mappedBy = "user")
    private Set<ApiLog> apiLogs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<CustomInfo> customInfos = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Device> devices = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<GroupMember> groupMembers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "owner")
    private Set<NestedGroup> nestedGroups = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Session> sessions = new LinkedHashSet<>();

    @OneToOne(mappedBy = "user")
    private UserInfo userInfo;


}