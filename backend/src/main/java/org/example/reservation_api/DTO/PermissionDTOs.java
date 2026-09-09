package org.example.reservation_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

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

        // Compact Constructor for applying defaults
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
     record CreateAttributeRequest(
            @NotBlank String entityTypeName,
            @NotBlank String attributeName
    ) implements PermissionDTOs {}

}
