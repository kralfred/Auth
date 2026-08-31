package org.example.reservation_api.config;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
public class APILogger {


    private final GenericRepository genericRepository;

    public APILogger(GenericRepository genericRepository) {
        this.genericRepository = genericRepository;
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
        UUID userId = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails userDetails) {
            // Extract UUID if user details or principal holds it, otherwise keep null for guests
            // userId = userDetails.getId();
        }

        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "GUEST";

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String eventType = className + "." + methodName;

        Object result = null;
        int status = 200;

        try {
            result = joinPoint.proceed();
            if (result instanceof ResponseEntity<?> responseEntity) {
                status = responseEntity.getStatusCode().value();
            }
            return result;
        } catch (IllegalArgumentException e) {
            status = 400; // Log validation/client errors as HTTP 400
            throw e;
        } catch (Exception e) {
            status = 500; // Log unexpected failures as HTTP 500
            throw e;
        } finally {
            long durationMs = System.currentTimeMillis() - start;
            Instant now = Instant.now();
            // 4. Persist Audit Log in 'finally' block so failed requests are also logged
            ApiLog log = new ApiLog(
                    eventType,
                    httpMethod,
                    path,
                    status,
                    durationMs,
                    userId,
                    Timestamp.from(now)
            );

            genericRepository.save("api_log",log);
        }
    }
}
