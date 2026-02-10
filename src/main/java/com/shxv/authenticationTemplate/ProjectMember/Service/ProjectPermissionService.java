package com.shxv.authenticationTemplate.ProjectMember.Service;

import com.shxv.authenticationTemplate.ProjectMember.DTO.DelegatePermissionRequest;
import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionResponse;
import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionUpdate;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectPermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProjectPermissionService {

    Flux<ProjectPermission> getAllPermission();
    Mono<List<PermissionResponse>> saveUserMemberPermissions(List<UUID> permissions, UUID projectMemberId);
    Mono<List<PermissionResponse>> saveGroupMemberPermissions(List<UUID> permissions, UUID projectMemberId);
    Flux<PermissionResponse> getUserMemberPermissions(UUID projectMemberId);
    Flux<PermissionResponse> getGroupMemberPermissions(UUID projectMemberId);
    Mono<List<PermissionResponse>> updateMemberPermissions(UUID projectMemberId, PermissionUpdate permissionUpdate);
    Mono<List<PermissionResponse>> setDelegatedPermissions(DelegatePermissionRequest delegatePermissionRequest);
    Flux<PermissionResponse> getDelegatedPermissions(UUID delegatedToUserId);
    Flux<ProjectPermission> getDelegatedPermissionByProject(UUID delegatedToUserId, UUID projectId);
    Mono<List<PermissionResponse>> revokeDelegatedPermission(UUID delegationId);
}
