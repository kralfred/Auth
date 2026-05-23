package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.NestedUserGroup;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.*;

@Repository
public class GroupRepository {

    private final JdbcTemplate jdbcTemplate;

    public GroupRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }



    /**
     * Helper to convert PostgreSQL UUID arrays to Java Sets
     */
    private Set<UUID> mapSqlArrayToSet(java.sql.Array sqlArray) throws SQLException {
        if (sqlArray == null) return new HashSet<>();
        UUID[] array = (UUID[]) sqlArray.getArray();
        return new HashSet<>(Arrays.asList(array));
    }

    /**
     * Calls a stored procedure to perform batch role updates
     */
    public void batchUpdateGroupRoles(UUID groupId, Set<UUID> userIds, UUID roleId) {
        String sql = "CALL p_update_group_members_role(?, ?, ?)";
        try {
            java.sql.Array userArray = jdbcTemplate.getDataSource().getConnection()
                    .createArrayOf("UUID", userIds.toArray());

            jdbcTemplate.update(sql, groupId, userArray, roleId);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group roles", e);
        }
    }
}