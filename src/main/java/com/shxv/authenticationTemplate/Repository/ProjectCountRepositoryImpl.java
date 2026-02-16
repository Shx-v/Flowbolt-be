package com.shxv.authenticationTemplate.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class ProjectCountRepositoryImpl implements ProjectCountRepositoryCustom {

    @Autowired
    DatabaseClient db;

    public ProjectCountRepositoryImpl(DatabaseClient db) {
        this.db = db;
    }

    @Override
    public Mono<Integer> incrementCount(UUID projectId) {

        String sql = """
            INSERT INTO project_count (project, ticket_count)
            VALUES (:projectId, 1)
            ON CONFLICT (project)
            DO UPDATE SET 
                ticket_count = project_count.ticket_count + 1,
                updated_at = CURRENT_TIMESTAMP
            RETURNING ticket_count
        """;

        return db.sql(sql)
                .bind("projectId", projectId)
                .map(row -> row.get("ticket_count", Integer.class))
                .one();
    }
}
