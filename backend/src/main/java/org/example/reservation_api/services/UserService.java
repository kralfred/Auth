package org.example.reservation_api.services;


import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.RegistrationResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final NestedGroupRepository nestedGroupRepository;
    private final GenericRepository genericRepository;
    private final InviteCodeService inviteCodeService;
    private final SystemSettingRepository systemSettingRepository;


    public UserService(
            UserRepository userRepository,
            NestedGroupRepository nestedGroupRepository,
            PasswordEncoder passwordEncoder,
            GenericRepository genericRepository,
            InviteCodeService inviteCodeService, SystemSettingRepository systemSettingRepository
    ) {
        this.userRepository = userRepository;
        this.nestedGroupRepository = nestedGroupRepository;
        this.passwordEncoder = passwordEncoder;
        this.genericRepository = genericRepository;
        this.inviteCodeService = inviteCodeService;

        this.systemSettingRepository = systemSettingRepository;
    }

    @Transactional
    public RegistrationResponse tryRegister(RegistrationRequest request) {

        // 1. Check for existing email
        if (userRepository.checkExistingEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }
        log.error("Email check ok for username", request.username());
        UUID targetGroupId;

        Optional<UUID> invitedGroupId = (request.inviteCode() != null && !request.inviteCode().isBlank())
                ? inviteCodeService.validateAndGetTargetGroupId(request.inviteCode())
                : Optional.empty();

        if (invitedGroupId.isPresent()) {
            targetGroupId = invitedGroupId.get();
        } else {
            targetGroupId = systemSettingRepository.getUuidValueByKey("DEFAULT_REGISTRATION_GROUP_ID");
        }

        log.error("Invite code ok for " + targetGroupId.toString());

        User user = new User(request.username(), targetGroupId);
        genericRepository.save("user", user);
        log.error("User Saved ", request.username(), targetGroupId);
        // 4. Step 2: Handle workspace creation if new owner


        // 5. Save associated user identity & credentials
        String hashedPassword = passwordEncoder.encode(request.password());
        UserInfo userInfo = new UserInfo(user.id(), request.email(), request.name(), hashedPassword);
        genericRepository.save("user_info", userInfo);
        log.error("User Info Saved ", request.username(), targetGroupId);
        UserIdentity identity = new UserIdentity(user.id(), "LOCAL", request.username());
        genericRepository.save("user_identity", identity);
        log.error("User identity Saved ", request.username(), targetGroupId);
        log.info("Successfully registered user {} into group {}", request.username(), targetGroupId);

        return new RegistrationResponse(user.id(), targetGroupId);
    }
}