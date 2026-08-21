package org.example.reservation_api.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.reservation_api.entities.ApiLog;
import org.example.reservation_api.repositories.APILogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

@Aspect
@Component
public class APILogger {

    @Autowired
    private APILogRepository auditLogRepository;

    @Around("execution(* org.example.reservation_api.controllers.*.*(..))")
    public Object logStep(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName()))
                ? auth.getName()
                : "GUEST";

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullAction = className + "." + methodName;

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            ApiLog log = new ApiLog();
            auditLogRepository.save(log);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            e.printStackTrace();

            ApiLog log = new ApiLog();
            auditLogRepository.save(log);
            throw e;
        }
    }
}
