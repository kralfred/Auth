package org.example.reservation_api.controllers;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.ApiLog;
import org.example.reservation_api.repositories.APILogRepository;
import org.example.reservation_api.repositories.GenericRepository;
import org.example.reservation_api.services.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static liquibase.Scope.Attr.logService;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class ApiLogController {

    private final APILogRepository apiLogRepository;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('view_logs', 'ROLE_ADMIN', 'ADMIN')")
    public ResponseEntity<List<ApiLog>> getLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer status
    ) {
        List<ApiLog> logs = apiLogRepository.findLogs(page, size, eventType, status);
        return ResponseEntity.ok(logs);
    }
}