package com.shxv.authenticationTemplate.Group.Service;

import com.shxv.authenticationTemplate.Group.DTO.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GroupService {

    Flux<GroupListResponse> getGroupList();
    Mono<GroupResponse> createGroup(GroupCreate groupCreate);
    Mono<GroupResponse> getGroupById(UUID groupId);
    Mono<GroupDetailResponse> getGroupDetailsById(UUID groupId);
    Flux<GroupResponse> getAllGroups();
    Flux<GroupResponse> getMyGroups();
    Mono<GroupResponse> updateGroup(UUID groupId, GroupUpdate groupUpdate);
    Mono<GroupResponse> transferLeadership(UUID groupId, UUID memberId);
    Mono<GroupResponse> leaveGroup(UUID groupId);
    Mono<GroupResponse> archiveGroup(UUID groupId);
    Mono<GroupResponse> restoreGroup(UUID groupId);
}
