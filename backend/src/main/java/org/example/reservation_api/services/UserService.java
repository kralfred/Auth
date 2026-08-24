package org.example.reservation_api.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.LogManager;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.RegistrationResponse;
import org.example.reservation_api.DTO.UserListResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.projections.GlobalCapabilityProjection;
import org.example.reservation_api.projections.UserCredentialsProjection;
import org.example.reservation_api.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class UserService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final GenericRepository genericRepository;



    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            TokenRepository tokenRepository,
            GenericRepository genericRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenRepository = tokenRepository;
        this.genericRepository = genericRepository;
    }




    public RegistrationResponse tryRegister(RegistrationRequest request) {


        if (userRepository.checkExistingEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        UUID defaultGroupId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID targetGroupId;

        if (request.inviteCode() != null && !request.inviteCode().isBlank()) {
            targetGroupId = tokenRepository.findByCodeAndActiveTrue(request.inviteCode())
                    .orElseGet(() -> {
                        log.warn("\u001B[33m⚠️ Invite code '{}' not found or inactive. Falling back to default group.\u001B[0m", request.inviteCode());
                        return defaultGroupId; // FIXED: Use defaultGroupId instead of randomUUID()
                    });
        } else {
            targetGroupId = defaultGroupId;
        }
        User user = new User(request.username(), targetGroupId);
        genericRepository.save("user", user);
        log.error("first user {} with groupId: {}", request.username(), targetGroupId);
        String hashedPassword = passwordEncoder.encode(request.password());



        UserInfo userInfo = new UserInfo(UUID.fromString("00000000-0000-0000-0000-000000000000"),request.email(),request.name(),hashedPassword);
        genericRepository.save("user_info", userInfo);
        log.error("second user {} with groupId: {}", request.username(), targetGroupId);
        UserIdentity identity = new UserIdentity(user.id(),"LOCAL",request.username());
        genericRepository.save("user_identity", identity);

        log.error("Registering user {} with groupId: {}", request.username(), targetGroupId);

        return new RegistrationResponse(user.id(), targetGroupId);
    }

}