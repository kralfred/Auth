package org.example.reservation_api.repositories;

import lombok.RequiredArgsConstructor;
import org.example.reservation_api.entities.User;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
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


}
