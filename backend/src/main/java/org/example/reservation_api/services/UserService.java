package org.example.reservation_api.services;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.RegistrationResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NestedGroupRepository nestedGroupRepository;
    private final GenericRepository genericRepository;
    private final InviteCodeService inviteCodeService;

    public UserService(
            UserRepository userRepository,
            NestedGroupRepository nestedGroupRepository,
            PasswordEncoder passwordEncoder,
            GenericRepository genericRepository,
            InviteCodeService inviteCodeService
    ) {
        this.userRepository = userRepository;
        this.nestedGroupRepository = nestedGroupRepository;
        this.passwordEncoder = passwordEncoder;
        this.genericRepository = genericRepository;
        this.inviteCodeService = inviteCodeService;
    }

    @Transactional
    public RegistrationResponse tryRegister(RegistrationRequest request) {

        // 1. Check for existing email
        if (userRepository.checkExistingEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        UUID targetGroupId;
        boolean isNewGroupOwner = false;

        // 2. Resolve target group context (Invite vs. Fresh Owner)
        if (request.inviteCode() != null && !request.inviteCode().isBlank()) {
            targetGroupId = inviteCodeService.validateAndGetTargetGroupId(request.inviteCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid or inactive invite code"));
        } else {
            targetGroupId = UUID.randomUUID();
            isNewGroupOwner = true;
        }

        // 3. Step 1: Save User initial record
        // default_group_id and current_environment set to targetGroupId
        User user = new User(request.username(), targetGroupId);
        genericRepository.save("user", user);

        // 4. Step 2: Handle workspace creation if new owner
        if (isNewGroupOwner) {
            NestedGroup newGroup = new NestedGroup(
                    targetGroupId,           // id
                    request.username() + "'s Group", // name
                    null,                    // parentGroupId (root group)
                    user.id()                // owner
            );
            nestedGroupRepository.save(newGroup);

            // Automatically generate an initial invite code for their new group
            inviteCodeService.createInviteForGroup(targetGroupId);
        }

        // 5. Save associated user identity & credentials
        String hashedPassword = passwordEncoder.encode(request.password());
        UserInfo userInfo = new UserInfo(user.id(), request.email(), request.name(), hashedPassword);
        genericRepository.save("user_info", userInfo);

        UserIdentity identity = new UserIdentity(user.id(), "LOCAL", request.username());
        genericRepository.save("user_identity", identity);

        log.info("Successfully registered user {} into group {}", request.username(), targetGroupId);

        return new RegistrationResponse(user.id(), targetGroupId);
    }
}