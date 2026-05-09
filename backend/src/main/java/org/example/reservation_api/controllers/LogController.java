package org.example.reservation_api.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static liquibase.Scope.Attr.logService;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    @GetMapping
    @PreAuthorize("hasAuthority('can_view_logs') or hasRole('ADMIN')")
    public List<LogEntry> getLogs() {
        return logService.findAll();
    }
}