package org.example.reservation_api.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.reservation_api.DTO.ErrorDetails;
import org.example.reservation_api.entities.ApiLog;
import org.example.reservation_api.repositories.APILogRepository;
import org.example.reservation_api.repositories.GenericRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;



@Aspect
@Component
@Slf4j
public class APILogger {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final APILogRepository logRepository;

    public APILogger(APILogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Around("execution(* org.example.reservation_api.controllers.*.*(..))")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Object logStep(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (attributes != null) ? attributes.getRequest() : null;

        String httpMethod = (request != null) ? request.getMethod() : "UNKNOWN";
        String path = (request != null) ? request.getRequestURI() : "UNKNOWN";

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID userId = null; // Map user ID from auth context if available

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String eventType = className + "." + methodName;

        Object result = null;
        int status = 200;
        ErrorDetails errorDetails = null;

        try {
            result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                status = responseEntity.getStatusCode().value();
            }
            return result;
        } catch (IllegalArgumentException e) {
            status = 400;
            errorDetails = new ErrorDetails("INVALID_ARGUMENT", e.getClass().getName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            status = 500;
            errorDetails = new ErrorDetails("INTERNAL_SERVER_ERROR", e.getClass().getName(), e.getMessage());
            throw e;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            Instant now = Instant.now();

            String errorDetailsJson = null;
            if (errorDetails != null) {
                try {
                    errorDetailsJson = objectMapper.writeValueAsString(errorDetails);
                } catch (JsonProcessingException e) {
                    errorDetailsJson = errorDetails.toString();
                }
            }


            ApiLog apiLog = new ApiLog(
                    eventType,
                    httpMethod,
                    path,
                    status,
                    durationMs,
                    userId,
                    Timestamp.from(now),
                    errorDetailsJson
            );

            try {
                logRepository.saveLog(apiLog);
            } catch (Exception logEx) {
                log.error("Failed to persist API log: {}", logEx.getMessage());
            }
        }
    }
}
