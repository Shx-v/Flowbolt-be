package com.shxv.flowbolt.ProjectMember.Service;

import com.shxv.flowbolt.Auth.DTO.UserListResponse;
import com.shxv.flowbolt.ProjectMember.DTO.PermissionResponse;
import com.shxv.flowbolt.ProjectMember.DTO.PermissionUpdate;
import com.shxv.flowbolt.ProjectMember.DTO.ProjectMemberCreate;
import com.shxv.flowbolt.ProjectMember.DTO.ProjectMemberResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProjectMemberService {

    Mono<ProjectMemberResponse> createProjectMember(ProjectMemberCreate projectMemberCreate);
    Flux<ProjectMemberResponse> getProjectMembers(UUID projectId);
    Mono<ProjectMemberResponse> getProjectMember(UUID projectId, UUID memberId);
    Mono<ProjectMemberResponse> removeMember(UUID projectMemberId);
    Mono<Boolean> isMember(UUID projectId, UUID userId);
    Flux<UserListResponse> getMemberUsers(UUID projectId);
    Flux<PermissionResponse> saveMemberPermissions(List<UUID> permissions, UUID projectMemberId);
    Flux<PermissionResponse> getMemberPermissions(UUID projectMemberId);
    Mono<List<PermissionResponse>> updateMemberPermissions(UUID projectMemberId, PermissionUpdate permissionUpdate);
}
