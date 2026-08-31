package org.example.reservation_api.services;

import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.GroupInvite;
import org.example.reservation_api.entities.GroupInviteTarget;
import org.example.reservation_api.repositories.GenericRepository;
import org.example.reservation_api.repositories.GroupInviteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class InviteCodeService {

    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final GenericRepository genericRepository;
    private final GroupInviteRepository groupInviteRepository;

    public InviteCodeService(GenericRepository genericRepository, GroupInviteRepository groupInviteRepository) {
        this.genericRepository = genericRepository;
        this.groupInviteRepository = groupInviteRepository;
    }

    /**
     * Validates an invite code and returns the primary target group ID.
     */
    public Optional<UUID> validateAndGetTargetGroupId(String code) {
        if (code == null || code.isBlank()) {
            log.error("No code found ");
            return Optional.empty();
        }
        log.error("No code found ");
        Optional<UUID> returnVal = groupInviteRepository.findPrimaryGroupIdByActiveCode(code.trim().toUpperCase());
        log.error("return val " + returnVal.toString());
        return returnVal;
    }

    /**
     * Creates an active invite code targeting a specific nested group.
     */
    @Transactional
    public GroupInvite createInviteForGroup(UUID nestedGroupId) {
        UUID inviteId = UUID.randomUUID();
        String uniqueCode = generateRandomCode(8);

        // 1. Create and save the main invite record
        GroupInvite invite = new GroupInvite(inviteId, uniqueCode, true);
        genericRepository.save("group_invite", invite);

        // 2. Create and save the target link mapping
        GroupInviteTarget target = new GroupInviteTarget(inviteId, nestedGroupId, true);
        genericRepository.save("group_invite_target", target);

        return invite;
    }

    /**
     * Deactivates an invite code so it can no longer be used.
     */
    @Transactional
    public void revokeInvite(UUID inviteId) {
        groupInviteRepository.deactivateInvite(inviteId);
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHA_NUMERIC.charAt(RANDOM.nextInt(ALPHA_NUMERIC.length())));
        }
        return sb.toString();
    }
}
