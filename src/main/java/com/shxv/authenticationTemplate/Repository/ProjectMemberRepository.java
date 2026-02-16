package com.shxv.authenticationTemplate.Repository;

import com.shxv.authenticationTemplate.Model.ProjectMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectMemberRepository extends ReactiveCrudRepository<ProjectMember, UUID> {

    @Query("SELECT * FROM project_member WHERE project = :projectId")
    Flux<ProjectMember> findByProject(UUID projectId);

    @Query("SELECT COUNT(*) > 0 FROM project_member WHERE project = :projectId AND member = :memberId")
    Mono<Boolean> existsByProjectAndMember(UUID projectId, UUID memberId);

    @Query("DELETE FROM project_member WHERE project = :projectId AND member = :memberId")
    Mono<Void> deleteByProjectAndMember(UUID projectId, UUID memberId);

    @Query("SELECT * FROM project_member WHERE project = :projectId AND member = :memberId")
    Mono<ProjectMember> findByProjectAndMember(UUID projectId, UUID memberId);

    @Query("SELECT * FROM project_member WHERE project = :projectId")
    Flux<ProjectMember> findAllByProject(UUID projectId);

    Flux<ProjectMember> findAllByMember(UUID memberId);

}
