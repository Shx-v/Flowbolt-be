package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectGroupMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectGroupMemberRepository extends ReactiveCrudRepository<ProjectGroupMember, UUID> {

    Mono<ProjectGroupMember> findByProjectIdAndGroupId(UUID projectId, UUID groupId);

    Flux<ProjectGroupMember> findByProjectId(UUID projectId);

    @Query("""
        SELECT group_id
        FROM project_group_members
        WHERE project_id = :projectId
          AND active = true
    """)
    Flux<UUID> findGroupIdsByProjectId(UUID projectId);

    Flux<ProjectGroupMember> findByGroupId(UUID groupId);

}
