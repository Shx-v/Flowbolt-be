package com.shxv.flowbolt.ProjectMember.Repository;

import com.shxv.flowbolt.ProjectMember.Model.ProjectMember;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectMemberRepository extends ReactiveCrudRepository<ProjectMember, UUID> {

    Mono<ProjectMember> findByProjectIdAndUserId(UUID projectId, UUID userId);

    Flux<ProjectMember> findByProjectId(UUID projectId);

    Mono<Boolean> existsByProjectIdAndUserId(UUID projectId, UUID userId);

    Flux<ProjectMember> findAllByUserId(UUID userId);

}
