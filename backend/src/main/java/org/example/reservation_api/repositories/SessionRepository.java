package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.Session;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends BaseRepository<Session> {

    // Used by SessionService to find active device sessions
    Optional<Session> findByUserIdAndDeviceIdAndIsActiveTrue(UUID userId, String deviceId);
}
