package org.example.reservation_api.controllers;

import org.example.reservation_api.entities.APILog;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.services.LogService;
import org.example.reservation_api.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static liquibase.Scope.Attr.logService;

@RestController
@RequestMapping("/api/logs")
public class LogController extends BaseController<APILog, LogService> {

    @Autowired
    public LogController(LogService service) {
        super(service);
    }

    // Use @Override to replace the BaseController's method
    @GetMapping
    @Override
    public ResponseEntity<List<APILog>> getAll() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("User Authorities: " + auth.getAuthorities());
        }
        return super.getAll();
    }
}