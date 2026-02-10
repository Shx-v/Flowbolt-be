package com.shxv.authenticationTemplate.Group.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Group.DTO.GroupMemberCreate;
import com.shxv.authenticationTemplate.Group.DTO.GroupMemberResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GroupMemberService {

    Mono<GroupMemberResponse> addMember(GroupMemberCreate groupMemberCreate, UUID groupId);
    Mono<List<UserListResponse>> addAllMember(List<GroupMemberCreate> groupMemberCreateList, UUID leaderId, UUID groupId);
    Flux<UserListResponse> getAllMemberByGroupId(UUID groupId);
    Mono<GroupMemberResponse> removeMember(UUID groupId, UUID memberId);
}
