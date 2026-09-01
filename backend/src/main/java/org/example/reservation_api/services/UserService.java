package org.example.reservation_api.services;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.DTO.RegistrationRequest;
import org.example.reservation_api.DTO.RegistrationResponse;
import org.example.reservation_api.entities.*;
import org.example.reservation_api.repositories.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
// 1. Change 'implements' to 'extends'
// 2. Specify the exact repository type (UserRepository) in the angle brackets
public class UserService extends BaseService<User, UserRepository> {

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
            InviteCodeService inviteCodeService,
            SystemSettingRepository systemSettingRepository
    ) {
        // 3. Pass userRepository up to BaseService via super()
        super(userRepository);

        this.userRepository = userRepository;
        this.nestedGroupRepository = nestedGroupRepository;
        this.passwordEncoder = passwordEncoder;
        this.genericRepository = genericRepository;
        this.inviteCodeService = inviteCodeService;
        this.systemSettingRepository = systemSettingRepository;
    }

    @Transactional(readOnly = true)
    public UUID getDefaultEnvironment(UUID id) {
        return userRepository.checkUsersDefaultEnvironment(id)
                .orElseThrow(() -> new IllegalStateException(
                        "No default environment ID found for user: " + id
                ));
    }

    @Transactional
    public RegistrationResponse tryRegister(RegistrationRequest request) {

        // 1. Check for existing email
        if (userRepository.checkExistingEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        UUID targetGroupId;

        Optional<UUID> invitedGroupId = (request.inviteCode() != null && !request.inviteCode().isBlank())
                ? inviteCodeService.validateAndGetTargetGroupId(request.inviteCode())
                : Optional.empty();

        if (invitedGroupId.isPresent()) {
            targetGroupId = invitedGroupId.get();
        } else {
            targetGroupId = systemSettingRepository.getUuidValueByKey("DEFAULT_REGISTRATION_GROUP_ID");
        }

        User user = new User(request.username(), targetGroupId);
        genericRepository.save("user", user);

        // 5. Save associated user identity & credentials
        String hashedPassword = passwordEncoder.encode(request.password());
        UserInfo userInfo = new UserInfo(user.id(), request.email(), request.name(), hashedPassword);
        genericRepository.save("user_info", userInfo);

        UserIdentity identity = new UserIdentity(user.id(), "LOCAL", request.username());
        genericRepository.save("user_identity", identity);

        return new RegistrationResponse(user.id(), targetGroupId);
    }
}