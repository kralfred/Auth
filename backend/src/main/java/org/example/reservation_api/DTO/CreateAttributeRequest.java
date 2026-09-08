package org.example.reservation_api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAttributeRequest(
        @NotBlank String entityTypeName,
        @NotBlank String attributeName
) {}


