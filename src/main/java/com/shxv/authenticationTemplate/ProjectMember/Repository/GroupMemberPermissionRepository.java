package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.GroupMemberPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface GroupMemberPermissionRepository extends ReactiveCrudRepository<GroupMemberPermission, UUID> {

    Mono<GroupMemberPermission> findByProjectGroupMemberIdAndPermissionId(UUID projectGroupMemberId, UUID permissionId);

    Flux<GroupMemberPermission> findAllByProjectGroupMemberIdAndActiveTrue(UUID projectGroupMemberId);

    Mono<Void> deleteByProjectGroupMemberId(UUID projectGroupMemberId);

}
