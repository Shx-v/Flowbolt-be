package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectUserMember;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectUserMemberRepository extends ReactiveCrudRepository<ProjectUserMember, UUID> {

    Mono<ProjectUserMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    Flux<ProjectUserMember> findByProjectId(UUID projectId);

    Mono<Boolean> existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Flux<ProjectUserMember> findAllByUserId(UUID userId);

}
