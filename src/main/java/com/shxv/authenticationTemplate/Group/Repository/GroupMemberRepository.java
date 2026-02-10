package com.shxv.authenticationTemplate.Group.Repository;

import com.shxv.authenticationTemplate.Group.Model.GroupMember;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface GroupMemberRepository extends ReactiveCrudRepository<GroupMember, UUID> {

    Flux<GroupMember> findAllByGroup(UUID group);

    Flux<GroupMember> findAllByMember(UUID member);

    @Query("""
        SELECT *
        FROM group_member
        WHERE group_id = :groupId
          AND member_id = :memberId
    """)
    Mono<GroupMember> findByGroupIdAndMemberId(UUID groupId, UUID memberId);

    @Query("""
        DELETE FROM group_member
        WHERE group_id = :groupId
          AND member_id = :memberId
    """)
    Mono<Void> deleteByGroupIdAndMemberId(UUID groupId, UUID memberId);

    Mono<Boolean> existsByGroupAndMember(UUID group, UUID member);

}
