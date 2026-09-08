package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class GroupRepository {

    private final JdbcClient jdbcClient;
    private final DataSource dataSource;

    private final GenericRepository genericRepository;

    public void addUsersToGroup(List<UUID> users, UUID userGroup){
        genericRepository.addGroupMembers("group_member",
                "group_id", userGroup, "user_id",users);
    }
    public void createNestedGroup(UUID newGroupId,String groupName,UUID parentGroupId,UUID ownerId){
      String sql = "INSERT INTO nested_group (id, name, parent_group_id, owner)" +
              "VALUES (:newGroupId, :groupName, :parentGroupId, :ownerId)";
      jdbcClient.sql(sql);
    }
    public void createUserGroup(UUID newGroupId,UUID groupId, String name){
        String sql = "INSERT INTO user_group (id, nested_group_id, name)" +
                "VALUES (:newGroupId, :groupId, :name)";
        jdbcClient.sql(sql);
    }

    public void addMembersToUserGroup(UUID groupId, List<UUID> userIds) {
        String sql = """
        INSERT INTO group_member (nested_group_id, user_id, group_id)
        VALUES (gen_random_uuid(), :userId, :groupId)
        ON CONFLICT (user_id, nested_group_id) DO NOTHING;
    """;

        // Convert list of userIds to batch parameter maps
        var batchParams = userIds.stream()
                .map(userId -> Map.of("userId", userId, "groupId", groupId))
                .toArray(Map[]::new);

        jdbcClient.sql(sql)
                .paramSource(batchParams)
                .update();
    }

}
