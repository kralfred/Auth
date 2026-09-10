package org.example.reservation_api.DTO;

import io.jsonwebtoken.Claims;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public sealed interface AuthDTOs {
    record LoginRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String deviceId,
            String deviceName,
            String deviceType
    ) implements AuthDTOs {}
     record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn,
            String refreshToken,
            long refreshTokenExpiration,
            UUID userId,
            String username,
            List<String> permissions
    ) implements AuthDTOs {
        public LoginResponse(String accessToken, long expiresIn, String refreshToken,long refreshTokenExpiration, UUID userId, String username, List<String> permissions) {
            this(accessToken, "Bearer", expiresIn, refreshToken,refreshTokenExpiration, userId, username, permissions);
        }
    }
    record RegistrationRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            String email,
            @NotBlank @Size(min = 3, max = 100) String password,
            String name,
            String inviteCode // Optional: null = create new workspace, present = join group
    ) implements AuthDTOs {}
    record RegistrationResponse(
            UUID userId,
            UUID defaultNestedGroupId
    ) implements AuthDTOs {}

    record AuthResponse(
            UserDto user,
            String accessToken,
            String tokenType,
            long expiresIn,
            String refreshToken
    ) implements AuthDTOs {


        public AuthResponse(UserDto user, String accessToken, String tokenType, long expiresIn) {
            this(user, accessToken, tokenType, expiresIn, null);
        }
    }
        record UserDto(
                UUID id,
                String username,
                String email,
                List<String> permissions
        ) implements AuthDTOs {}

    record TokenValidationResult(
            UUID tokenId,
            UUID userId,
            String username,
            List<String> permissions,
            Claims claims,
            ValidationStatus status
    ) implements AuthDTOs {
        public enum ValidationStatus {
            VALID, EXPIRED, INVALID
        }

        public boolean isValid() {
            return status == ValidationStatus.VALID;
        }
    }
}
