package org.example.reservation_api.DTO;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.reservation_api.DTO.AttributeMutationPermissionRequest;
import org.example.reservation_api.DTO.EntityCreationPermissionRequest;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "permissionType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = EntityCreationPermissionRequest.class, name = "ENTITY_CREATION"),
        @JsonSubTypes.Type(value = AttributeMutationPermissionRequest.class, name = "ATTRIBUTE_MUTATION")
})
public sealed interface CreatePermissionRequest
        permits EntityCreationPermissionRequest, AttributeMutationPermissionRequest {

    String action();
    String entityTypeName();
}
