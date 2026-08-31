package org.example.reservation_api.repositories;

import org.example.reservation_api.entities.NestedGroup;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NestedGroupRepository {

    private final GenericRepository genericRepository;
    private final JdbcClient jdbcClient;

    public NestedGroupRepository(GenericRepository genericRepository, JdbcClient jdbcClient) {
        this.genericRepository = genericRepository;
        this.jdbcClient = jdbcClient;
    }

    /**
     * Delegates basic saving directly to GenericRepository
     */
    public NestedGroup save(NestedGroup group) {
        return genericRepository.save("nested_group", group);
    }

    /**
     * Entity-specific query: Find all child groups of a parent group
     */
    public List<NestedGroup> findByParentGroupId(UUID parentGroupId) {
        String sql = "SELECT * FROM public.nested_group WHERE parent_group_id = :parentGroupId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", parentGroupId, Types.OTHER);

        return jdbcClient.sql(sql)
                .param(params) // 👈 Added Types.OTHER for PostgreSQL UUID
                .query(NestedGroup.class)
                .list();
    }
}





