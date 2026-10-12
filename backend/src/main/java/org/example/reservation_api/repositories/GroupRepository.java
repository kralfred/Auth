package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.reservation_api.entities.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.example.reservation_api.DTO.GroupDTOs.*;


import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GroupRepository {

    private final JdbcClient jdbcClient;
    private final DataSource dataSource;

    private final GenericRepository genericRepository;

    public void addUsersToGroup(List<UUID> users, UUID userGroup){
        genericRepository.addGroupMembers("group_member",
                "group_id", userGroup, "user_id",users);
    }

    public Optional<NestedGroupConfiguration> findNestedGroupConfiguration(UUID groupId) {
        String sql = """
        SELECT ngc.nested_group_id,
               ngc.default_user_group,
               ngc.max_members,
               ngc.base_url,
               ngc.refresh_token_time,
               ngc.access_token_time
        FROM nested_group_configuration ngc
        WHERE ngc.nested_group_id = ?
        """;

        return jdbcClient.sql(sql)
                .param(groupId)
                .query(NestedGroupConfiguration.class)
                .optional();
    }

    public void createNestedGroup(UUID newGroupId, String groupName, UUID parentGroupId, UUID ownerId) {
        String sql = """
        INSERT INTO nested_group (id, name, parent_group_id, owner)
        VALUES (:newGroupId, :groupName, :parentGroupId, :ownerId)
        """;

        jdbcClient.sql(sql)
                .param("newGroupId", newGroupId)
                .param("groupName", groupName)
                .param("parentGroupId", parentGroupId)
                .param("ownerId", ownerId)
                .update();
    }

    public void createUserGroup(UUID newGroupId, UUID groupId, String name) {
        String sql = """
        INSERT INTO user_group (id, nested_group_id, name)
        VALUES (:newGroupId, :groupId, :name)
        """;

        jdbcClient.sql(sql)
                .param("newGroupId", newGroupId)
                .param("groupId", groupId)
                .param("name", name)
                .update();
    }

    public void updateConfiguration(UUID groupId, NestedGroupConfiguration req) {
        log.info("updateConfiguration: groupId={}, maxMembers={}, baseUrl={}, refreshTokenTime={}, accessTokenTime={}",
                groupId, req.maxMembers(), req.baseUrl(), req.refreshTokenTime(), req.accessTokenTime());

        String sql = """
    UPDATE nested_group_configuration
    SET max_members        = COALESCE(?, max_members),
        base_url           = COALESCE(?, base_url),
        refresh_token_time = COALESCE(?, refresh_token_time),
        access_token_time  = COALESCE(?, access_token_time)
    WHERE nested_group_id = ?
    """;

        int affected = jdbcClient.sql(sql)
                .param(req.maxMembers())
                .param(req.baseUrl())
                .param(req.refreshTokenTime())
                .param(req.accessTokenTime())
                .param(groupId)
                .update();

        log.info("updateConfiguration: affected={}", affected);

        if (affected == 0) {
            throw new RuntimeException(
                    "No configuration found for nested group " + groupId);
        }
    }

    public String findCurrentNestedGroupName(UUID groupId){
        String sql = """ 
        SELECT name FROM nested_group WHERE nested_group.id = ?;
        """;
        return jdbcClient.sql(sql).param(groupId).query(String.class).single();
    }
    public Optional<Group> findById(UUID groupId) {
        String sql = """
            SELECT
                ng.id,
                ng.name,
                ng.parent_group_id,
                ng.owner,
                cfg.base_url,
                cfg.max_members,
                cfg.access_token_time,
                cfg.refresh_token_time
            FROM nested_group ng
            LEFT JOIN nested_group_configuration cfg
                   ON cfg.nested_group_id = ng.id
            WHERE ng.id = ?
            """;

        return jdbcClient.sql(sql)
                .param(groupId)
                .query(Group.class)
                .optional();
    }

    public List<Group> findAllByUser(UUID userId) {
        String sql = """
            SELECT DISTINCT
                ng.id,
                ng.name,
                ng.parent_group_id,
                ng.owner,
                cfg.base_url,
                cfg.max_members,
                cfg.access_token_time,
                cfg.refresh_token_time
            FROM group_member gm
            JOIN nested_group ng ON ng.id = gm.nested_group_id
            LEFT JOIN nested_group_configuration cfg
                   ON cfg.nested_group_id = ng.id
            WHERE gm.user_id = ?
            ORDER BY ng.name
            """;

        return jdbcClient.sql(sql)
                .param(userId)
                .query(Group.class)
                .list();
    }

    public void addMembersToUserGroup(UUID groupId, List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) return;

        String sql = """
        INSERT INTO group_member (nested_group_id, user_id, group_id)
        SELECT ug.nested_group_id, u.user_id, ug.id
        FROM user_group ug
        CROSS JOIN unnest(:userIds::uuid[]) AS u(user_id)
        WHERE ug.id = :groupId
        ON CONFLICT DO NOTHING
        """;

        jdbcClient.sql(sql)
                .param("groupId", groupId)
                .param("userIds", userIds.toArray(UUID[]::new))
                .update();
    }
    public void createDefaultConfiguration(UUID nestedGroupId, UUID defaultUserGroupId) {
        String sql = """
        INSERT INTO nested_group_configuration
            (nested_group_id, default_user_group, max_members,
             refresh_token_time, access_token_time)
        VALUES (?, ?, ?, ?, ?)
        """;

        jdbcClient.sql(sql)
                .param(nestedGroupId)
                .param(defaultUserGroupId)
                .param(1.1)
                .param(604800L)   // 7 days
                .param(900L)      // 15 minutes
                .update();
    }

}
