package com.shxv.authenticationTemplate.ProjectMember.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Group.DTO.ProjectWisePermission;
import com.shxv.authenticationTemplate.Group.Model.Group;
import com.shxv.authenticationTemplate.ProjectMember.DTO.ProjectMemberCreate;
import com.shxv.authenticationTemplate.ProjectMember.DTO.ProjectMemberResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectMemberService {

    Mono<ProjectMemberResponse> createProjectMember(ProjectMemberCreate projectMemberCreate);
    Flux<ProjectMemberResponse> getProjectMembers(UUID projectId);
    Mono<ProjectMemberResponse> getProjectMember(UUID projectId, UUID memberId);
    Mono<ProjectMemberResponse> removeMember(UUID projectMemberId);
    Mono<Boolean> isMember(UUID projectId, UUID userId);
    Flux<UserListResponse> getMemberUsers(UUID projectId);
    Flux<ProjectWisePermission> getProjectWisePermission(Group group, Boolean isAdmin);
}
