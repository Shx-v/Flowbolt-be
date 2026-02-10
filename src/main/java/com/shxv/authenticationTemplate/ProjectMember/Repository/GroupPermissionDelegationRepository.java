package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.GroupPermissionDelegation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface GroupPermissionDelegationRepository extends ReactiveCrudRepository<GroupPermissionDelegation, UUID> {

    Flux<GroupPermissionDelegation> findByDelegatedToUserId(UUID userId);

    Mono<GroupPermissionDelegation> findByDelegatedToUserIdAndProjectGroupMemberPermissionId(UUID delegatedToUserId, UUID projectGroupMemberPermissionId);

}
