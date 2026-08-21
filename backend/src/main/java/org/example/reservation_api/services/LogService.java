package org.example.reservation_api.services;

import org.example.reservation_api.entities.ApiLog;
import org.example.reservation_api.repositories.APILogRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.springframework.stereotype.Service;


@Service
public class LogService extends BaseService<ApiLog, APILogRepository> {

    public LogService(APILogRepository logRepository) {
        super(logRepository);

    }


}


