package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.APILog;
import org.example.reservation_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends BaseRepository<APILog> {

}
