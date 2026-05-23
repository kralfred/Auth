package org.example.reservation_api.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.reservation_api.DTO.UserListResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.projections.GlobalCapabilityProjection;
import org.example.reservation_api.repositories.BaseRepository;
import org.example.reservation_api.repositories.PermissionRepository;
import org.example.reservation_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends BaseService<User, UserRepository> {

    private final AccessControlService accessControlService;

    public UserService(UserRepository userRepository, AccessControlService accessControlService) {
        super(userRepository);
        this.accessControlService = accessControlService;
    }

    // Clean CRUD: Just handle the user entity
    public User selfUpdateUser(String currentUsername, User updatedData) {
        return repository.findByUsername(currentUsername).map(existingUser -> {
            if (updatedData.getEmail() != null) {
                existingUser.setEmail(updatedData.getEmail());
            }
            return repository.dbUpdate(existingUser);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Delegate authorization checks
    public boolean canUserSeeLogs(UUID userId, UUID nestedGroupId) {
        return accessControlService.hasPermission(userId, nestedGroupId, "can_view_logs");
    }

}