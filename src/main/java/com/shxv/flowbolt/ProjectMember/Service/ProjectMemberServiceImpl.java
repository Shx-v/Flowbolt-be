package com.shxv.flowbolt.ProjectMember.Service;

import com.shxv.flowbolt.Auth.DTO.UserListResponse;
import com.shxv.flowbolt.Auth.Service.UserService;
import com.shxv.flowbolt.Auth.Util.UserRoleUtil;
import com.shxv.flowbolt.Project.Enum.ProjectStatus;
import com.shxv.flowbolt.Project.Service.ProjectService;
import com.shxv.flowbolt.ProjectMember.DTO.PermissionResponse;
import com.shxv.flowbolt.ProjectMember.DTO.PermissionUpdate;
import com.shxv.flowbolt.ProjectMember.DTO.ProjectMemberCreate;
import com.shxv.flowbolt.ProjectMember.DTO.ProjectMemberResponse;
import com.shxv.flowbolt.ProjectMember.Model.MemberPermission;
import com.shxv.flowbolt.ProjectMember.Model.ProjectMember;
import com.shxv.flowbolt.ProjectMember.Repository.MemberPermissionRepository;
import com.shxv.flowbolt.ProjectMember.Repository.ProjectMemberRepository;
import com.shxv.flowbolt.ProjectMember.Util.PermissionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    MemberPermissionRepository memberPermissionRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectPermissionService projectPermissionService;

    @Autowired
    PermissionResolver permissionResolver;

    @Autowired
    UserService userService;

    @Override
    public Mono<ProjectMemberResponse> createProjectMember(ProjectMemberCreate projectMemberCreate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectService.getProjectById(projectMemberCreate.getProject())
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this ID exists")))
                        .flatMap(project -> permissionResolver.hasPermission("ADD_MEMBER", adminCheckResponse.getUserId(), project.getId())
                                .flatMap(hasPermission -> {
                                    if (!project.getOwner().equals(adminCheckResponse.getUserId()) && !adminCheckResponse.getIsAdmin() && !hasPermission) {
                                        return Mono.error(
                                                new RuntimeException("You are not allowed to add member in this project")
                                        );
                                    }

                                    if (!ProjectStatus.ACTIVE.getLabel().equals(project.getStatus())) {
                                        return Mono.error(new RuntimeException("This project is not currently active"));
                                    }

                                    return userService.getUserById(projectMemberCreate.getMember())
                                            .switchIfEmpty(Mono.error(new RuntimeException("No user with this ID exists")))
                                            .flatMap(user -> projectMemberRepository.findByProjectIdAndUserId(
                                                                    project.getId(), user.getId()
                                                            )
                                                            .hasElement()
                                                            .flatMap(exists -> {
                                                                if (exists) {
                                                                    return Mono.error(new RuntimeException("User is already a member of this project"));
                                                                }

                                                                return projectMemberRepository.save(
                                                                                ProjectMember.builder()
                                                                                        .projectId(project.getId())
                                                                                        .userId(user.getId())
                                                                                        .active(true)
                                                                                        .createdAt(LocalDateTime.now())
                                                                                        .updatedAt(LocalDateTime.now())
                                                                                        .build())
                                                                        .flatMap(savedMember -> saveMemberPermissions(
                                                                                projectMemberCreate.getPermissions(), savedMember.getId()
                                                                        )
                                                                                .collectList()
                                                                                .flatMap(savedPerms -> mapToResponse(savedMember, savedPerms)));
                                                            })
                                            );
                                })))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<ProjectMemberResponse> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findByProjectId(projectId)
                .flatMap(this::mapToResponse);
    }

    @Override
    public Mono<ProjectMemberResponse> getProjectMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.findByProjectIdAndUserId(projectId, memberId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project member not found")))
                .flatMap(this::mapToResponse);
    }

    @Override
    public Mono<ProjectMemberResponse> removeMember(UUID projectMemberId) {

        return userRoleUtil.isAdmin()
                .flatMap(auth -> {

                    Mono<ProjectMemberResponse> removeUserMember =
                            projectMemberRepository.findById(projectMemberId)
                                    .flatMap(member ->
                                            projectService.getProjectById(member.getProjectId())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                                                    .flatMap(project -> permissionResolver.hasPermission("REMOVE_MEMBER", auth.getUserId(), project.getId())
                                                            .flatMap(hasPermission -> {
                                                                if (!auth.getIsAdmin()
                                                                        && !project.getOwner().equals(auth.getUserId())
                                                                        && !hasPermission) {
                                                                    return Mono.error(new RuntimeException("You are not allowed to remove this member"));
                                                                }

                                                                return projectMemberRepository
                                                                        .deleteById(projectMemberId)
                                                                        .then(mapToResponse(member));
                                                            }))
                                    );

                    return removeUserMember
                            .switchIfEmpty(Mono.error(new RuntimeException("Project member not found")));
                })
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Boolean> isMember(UUID projectId, UUID userId) {

        return projectMemberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .filter(ProjectMember::getActive)
                .hasElement();
    }

    @Override
    public Flux<UserListResponse> getMemberUsers(UUID projectId) {
        return projectService.getProjectById(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                .flatMapMany(project -> projectMemberRepository.findByProjectId(projectId)
                        .filter(ProjectMember::getActive)
                        .flatMap(projectUserMember -> userService.getUserById(projectUserMember.getUserId())
                                .map(userResponse -> UserListResponse.builder()
                                        .id(userResponse.getId())
                                        .username(userResponse.getUsername())
                                        .firstName(userResponse.getFirstName())
                                        .lastName(userResponse.getLastName())
                                        .build()
                                )
                        )
                );
    }

    @Override
    public Flux<PermissionResponse> saveMemberPermissions(List<UUID> permissions, UUID projectMemberId) {
        if (permissions == null || permissions.isEmpty()) {
            return Flux.empty();
        }

        return Flux.fromIterable(permissions)
                .flatMap(permissionId -> projectPermissionService.getProjectPermissionById(permissionId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid permission: " + permissionId)))
                        .flatMap(permission -> memberPermissionRepository.findByProjectUserMemberIdAndPermissionId(projectMemberId, permissionId)
                                .hasElement()
                                .flatMap(exists -> {
                                    if (exists) {
                                        return Mono.empty();
                                    }

                                    return memberPermissionRepository.save(
                                            MemberPermission.builder()
                                                    .projectUserMemberId(projectMemberId)
                                                    .permissionId(permissionId)
                                                    .active(true)
                                                    .createdAt(LocalDateTime.now())
                                                    .updatedAt(LocalDateTime.now())
                                                    .build()
                                    );
                                })
                        )
                )
                .flatMap(this::mapToPermResponse);
    }

    @Override
    public Flux<PermissionResponse> getMemberPermissions(UUID projectMemberId) {
        return memberPermissionRepository
                .findAllByProjectUserMemberIdAndActiveTrue(projectMemberId)
                .flatMap(this::mapToPermResponse);
    }

    @Override
    public Mono<List<PermissionResponse>> updateMemberPermissions(UUID projectMemberId, PermissionUpdate permissionUpdate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectMemberRepository.findById(projectMemberId)
                        .flatMap(userMember -> projectService.getProjectById(userMember.getProjectId())
                                .flatMap(project -> permissionResolver.hasPermission("UPDATE_PERMISSION", adminCheckResponse.getUserId(), project.getId())
                                        .flatMap(hasPermission -> {
                                            if (!adminCheckResponse.getIsAdmin() && !project.getOwner().equals(adminCheckResponse.getUserId()) && !hasPermission) {
                                                return Mono.error(new RuntimeException("You are not allowed to update member permissions"));
                                            }

                                            return updateUserMemberPermissions(projectMemberId, permissionUpdate.getPermissions());
                                        })
                                )
                        )
                )
                .as(transactionalOperator::transactional);
    }

    //HELPER METHODS
    private Mono<ProjectMemberResponse> mapToResponse(ProjectMember projectMember) {
        return Mono.zip(
                        projectService.getProjectById(projectMember.getProjectId()),
                        userService.getUserById(projectMember.getUserId()),
                        getMemberPermissions(projectMember.getId()).collectList()
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectMember.getId())
                                .project(tuple.getT1())
                                .user(tuple.getT2())
                                .member(projectMember.getUserId())
                                .permission(tuple.getT3())
                                .active(projectMember.getActive())
                                .createdAt(projectMember.getCreatedAt())
                                .updatedAt(projectMember.getUpdatedAt())
                                .build());
    }

    private Mono<ProjectMemberResponse> mapToResponse(ProjectMember projectMember, List<PermissionResponse> permissions) {
        return Mono.zip(
                        projectService.getProjectById(projectMember.getProjectId()),
                        userService.getUserById(projectMember.getUserId())
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectMember.getId())
                                .project(tuple.getT1())
                                .user(tuple.getT2())
                                .member(projectMember.getUserId())
                                .active(projectMember.getActive())
                                .permission(permissions)
                                .createdAt(projectMember.getCreatedAt())
                                .updatedAt(projectMember.getUpdatedAt())
                                .build());
    }

    private Mono<PermissionResponse> mapToPermResponse(MemberPermission memberPermission) {
        return projectPermissionService.getProjectPermissionById(memberPermission.getPermissionId())
                .map(permission -> PermissionResponse.builder()
                        .key(permission.getKey())
                        .description(permission.getDescription())
                        .build()
                );
    }

    private Mono<List<PermissionResponse>> updateUserMemberPermissions(UUID projectUserMemberId, List<UUID> permissions) {
        return memberPermissionRepository.deleteByProjectUserMemberId(projectUserMemberId)
                .thenMany(Flux.fromIterable(permissions))
                .flatMap(permissionId ->
                        memberPermissionRepository.save(
                                MemberPermission.builder()
                                        .projectUserMemberId(projectUserMemberId)
                                        .permissionId(permissionId)
                                        .active(true)
                                        .build()
                        )
                )
                .flatMap(this::mapToPermResponse)
                .collectList();
    }

}
