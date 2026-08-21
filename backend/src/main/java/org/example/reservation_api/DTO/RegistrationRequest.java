package org.example.reservation_api.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RegistrationRequest {
        private String username;
        private String email;
        private String name;
        private String password;
}
