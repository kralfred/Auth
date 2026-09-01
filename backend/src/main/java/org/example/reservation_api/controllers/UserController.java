package org.example.reservation_api.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.User;
import org.example.reservation_api.repositories.AbstractJdbcRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.example.reservation_api.services.BaseService;
import org.example.reservation_api.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User> {

    public UserController(UserService userService) {
        super(userService);
    }
}