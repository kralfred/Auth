package org.example.reservation_api.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class PermissionAttributeId implements Serializable {
    private static final long serialVersionUID = 2549029524727593688L;
    @NotNull
    @Column(name = "permission_id", nullable = false)
    private UUID permissionId;

    @NotNull
    @Column(name = "targetable_attribute_id", nullable = false)
    private UUID targetableAttributeId;


}