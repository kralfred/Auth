package org.example.reservation_api.projections;

import java.util.UUID;

public interface UserCredentialsProjection {
    UUID getUserId();
    String getUsername();
    String getPasswordHash();
    UUID getCurrentEnvironment(); // Retrieves saved current_environment UUID
}
