package org.example.reservation_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.aspectj.weaver.ast.Not;

import java.util.List;
import java.util.UUID;

public sealed interface PermissionDTOs {
    record CreatePermissionRequest(
            String permissionName,

            @NotNull
            UUID actionId,

            @NotNull
            UUID nestedGroupId,

            List<UUID> targetableAttributeIds,

            Boolean isRequired,

            String autoFillValue
    ) implements PermissionDTOs {


        public CreatePermissionRequest {
            if (permissionName == null || permissionName.isBlank()) {
                permissionName = "UNNAMED_PERMISSION_" + UUID.randomUUID().toString().substring(0, 8);
            }
            if (targetableAttributeIds == null) {
                targetableAttributeIds = List.of(); // Safe immutable empty list
            }
            if (isRequired == null) {
                isRequired = false;
            }
            if (autoFillValue == null) {
                autoFillValue = "";
            }
        }
    }
    record AppendAttributeToNestedGroupRequest(
            @NotNull()UUID nestedGroupId,
            @NotNull()List<UUID> attributeIds
    )
            implements PermissionDTOs{}

    record CreateAttributeForEntityRequest(
            @NotBlank(message = "Attribute name is required") String name,
            @NotNull()UUID entityType
    ) implements PermissionDTOs{}

    record SimpleCreateResponse(
            UUID newId
    ){}
    record EntityAttribute(
            @NotBlank UUID id,
            String name
    ) implements PermissionDTOs{}

     record CreateEntityRequest(
             @NotBlank(message = "Entity name is required") String name
    ) implements PermissionDTOs {}

}
