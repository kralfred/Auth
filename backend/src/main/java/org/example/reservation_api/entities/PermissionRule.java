package org.example.reservation_api.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "permission_group_rules")
public class PermissionRule extends BaseEntity {

    @Column(name = "permission_name")
    private String permissionName;

    @ManyToOne
    @JoinColumn(name = "target_entity_group_id")
    private EntityGroup targetEntityGroup;
}
