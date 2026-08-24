package org.example.reservation_api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        String name,
        String inviteCode // Optional: null = create new workspace, present = join group
) {}
