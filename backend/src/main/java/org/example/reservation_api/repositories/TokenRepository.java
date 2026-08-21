package org.example.reservation_api.repositories;


import org.example.reservation_api.entities.RefreshToken;
import org.example.reservation_api.entities.Session;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends BaseRepository<RefreshToken> {

    // Find a token entry by its unique hash
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    // Fetch the parent active session directly via the token's hash
    @Query("SELECT r.session FROM RefreshToken r WHERE r.tokenHash = :hash AND r.isRevoked = false AND r.session.isActive = true")
    Optional<Session> findActiveSessionByRefreshTokenHash(@Param("hash") String hash);

    // Revoke a single token by its hash
    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.tokenHash = :hash")
    void revokeByHash(@Param("hash") String hash);

    // Revoke all refresh tokens attached to a specific session (used during device login re-authentication)
    @Modifying
    @Query("UPDATE RefreshToken r SET r.isRevoked = true WHERE r.session.id = :sessionId AND r.isRevoked = false")
    void revokeAllBySessionId(@Param("sessionId") UUID sessionId);
}


