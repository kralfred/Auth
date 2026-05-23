package org.example.reservation_api.services;

import org.example.reservation_api.entities.APILog;
import org.example.reservation_api.repositories.AuditLogRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class LogService extends BaseService<APILog, AuditLogRepository> {

    public LogService(AuditLogRepository logRepository) {
        super(logRepository);

    }


}


