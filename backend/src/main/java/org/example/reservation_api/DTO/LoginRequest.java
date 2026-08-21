package org.example.reservation_api.DTO;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;


public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String deviceId,     // Unique client-side generated UUID for the device
        String deviceName,             // e.g., "Chrome on macOS", "iPhone 15"
        String deviceType              // e.g., "WEB", "MOBILE"
) {}
