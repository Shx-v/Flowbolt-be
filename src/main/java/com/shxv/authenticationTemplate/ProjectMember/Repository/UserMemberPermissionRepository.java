package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.UserMemberPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserMemberPermissionRepository extends ReactiveCrudRepository<UserMemberPermission, UUID> {

    Mono<UserMemberPermission> findByProjectUserMemberIdAndPermissionId(UUID projectUserMemberId, UUID permissionId);

    Flux<UserMemberPermission> findAllByProjectUserMemberIdAndActiveTrue(UUID projectUserMemberId);

    Mono<Void> deleteByProjectUserMemberId(UUID projectUserMemberId);

}
