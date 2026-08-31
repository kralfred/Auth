package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.GroupInvite;
import org.example.reservation_api.entities.GroupInviteTarget;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GroupInviteRepository {

    private final GenericRepository genericRepository;
    private final JdbcClient jdbcClient;

    public GroupInviteRepository(GenericRepository genericRepository, JdbcClient jdbcClient) {
        this.genericRepository = genericRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Saves a GroupInvite entity using GenericRepository.
     */
    public GroupInvite saveInvite(GroupInvite invite) {
        return genericRepository.save("group_invite", invite);
    }

    /**
     * Saves a GroupInviteTarget mapping using GenericRepository.
     */
    public GroupInviteTarget saveInviteTarget(GroupInviteTarget target) {
        return genericRepository.save("group_invite_target", target);
    }

    /**
     * Finds the primary nested_group_id associated with an active invite code.
     */
    public Optional<UUID> findPrimaryGroupIdByActiveCode(String code) {
        String sql = """
            SELECT t.nested_group_id
            FROM public.group_invite i
            JOIN public.group_invite_target t ON i.id = t.invite_id
            WHERE i.code = :code
              AND i.is_active = true
              AND t.is_primary = true
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("code", code, Types.VARCHAR);

        return jdbcClient.sql(sql)
                .paramSource(params)
                .query(UUID.class)
                .optional();
    }

    /**
     * Deactivates an invite code by ID.
     */
    public void deactivateInvite(UUID inviteId) {
        String sql = "UPDATE public.group_invite SET is_active = false WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", inviteId, Types.OTHER);

        jdbcClient.sql(sql)
                .paramSource(params)
                .update();
    }
}