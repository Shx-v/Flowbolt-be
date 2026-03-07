package com.shxv.flowbolt.ProjectMember.Repository;

import com.shxv.flowbolt.ProjectMember.Model.MemberPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface MemberPermissionRepository extends ReactiveCrudRepository<MemberPermission, UUID> {

    Mono<MemberPermission> findByProjectUserMemberIdAndPermissionId(UUID projectUserMemberId, UUID permissionId);

    Flux<MemberPermission> findAllByProjectUserMemberIdAndActiveTrue(UUID projectUserMemberId);

    Mono<Void> deleteByProjectUserMemberId(UUID projectUserMemberId);

}
