package org.example.reservation_api.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.UserListResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.projections.GlobalCapabilityProjection;
import org.example.reservation_api.repositories.BaseRepository;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseService<User, UserRepository> {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String tryRegister(RegistrationRequest request) {
        User newUser = new User();
        UserInfo cred = new UserInfo();
        newUser.setUsername(request.getUsername());
        cred.setPassword(passwordEncoder.encode(request.getPassword()));

        Set<String> perms = new HashSet<>();
        perms.add("read_own_profile");

        userRepository.registerUser(request.getUsername(), request.getEmail(),request.getName(), request.getPassword());
        return "User registered successfully";
    }

    @Transactional
    public void switchEnvironment(UUID userId, UUID newNestedGroupId) {
        userRepository.updateCurrentEnvironment(userId, newNestedGroupId);
    }
}