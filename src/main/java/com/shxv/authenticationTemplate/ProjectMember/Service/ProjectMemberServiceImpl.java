package com.shxv.authenticationTemplate.ProjectMember.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Group.DTO.ProjectWisePermission;
import com.shxv.authenticationTemplate.Group.DTO.UserPermission;
import com.shxv.authenticationTemplate.Group.Model.Group;
import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Group.Repository.GroupRepository;
import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionResponse;
import com.shxv.authenticationTemplate.ProjectMember.DTO.ProjectMemberCreate;
import com.shxv.authenticationTemplate.ProjectMember.DTO.ProjectMemberResponse;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectGroupMember;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectUserMember;
import com.shxv.authenticationTemplate.ProjectMember.Repository.GroupMemberPermissionRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectGroupMemberRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectPermissionRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectUserMemberRepository;
import com.shxv.authenticationTemplate.ProjectMember.Util.PermissionResolver;
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
    ProjectGroupMemberRepository projectGroupMemberRepository;

    @Autowired
    ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectPermissionService projectPermissionService;

    @Autowired
    PermissionResolver permissionResolver;

    @Autowired
    UserService userService;

    @Autowired
    GroupMemberPermissionRepository groupMemberPermissionRepository;

    @Autowired
    ProjectPermissionRepository projectPermissionRepository;

    @Override
    public Mono<ProjectMemberResponse> createProjectMember(ProjectMemberCreate projectMemberCreate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(projectMemberCreate.getProject())
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this ID exists")))
                        .flatMap(project -> permissionResolver.hasPermission("ADD_MEMBER", adminCheckResponse.getUserId(), project.getId())
                                .flatMap(hasPermission -> {
                                    if (!project.getOwner().equals(adminCheckResponse.getUserId()) && !adminCheckResponse.getIsAdmin() && !hasPermission) {
                                        return Mono.error(
                                                new RuntimeException("You are not allowed to add member in this project")
                                        );
                                    }

                                    if (!project.getStatus().equals(ProjectStatus.ACTIVE)) {
                                        return Mono.error(
                                                new RuntimeException("This project is not currently active")
                                        );
                                    }

                                    if (projectMemberCreate.getIsGroup()) {
                                        return groupRepository.findById(projectMemberCreate.getMember())
                                                .switchIfEmpty(Mono.error(new RuntimeException("No group with this ID exists")))
                                                .flatMap(group -> projectGroupMemberRepository.findByProjectIdAndGroupId(
                                                                        project.getId(), group.getId()
                                                                )
                                                                .hasElement()
                                                                .flatMap(exists -> {
                                                                    if (exists) {
                                                                        return Mono.error(new RuntimeException("Group is already a member of this project"));
                                                                    }

                                                                    return projectGroupMemberRepository.save(
                                                                                    ProjectGroupMember.builder()
                                                                                            .projectId(project.getId())
                                                                                            .groupId(group.getId())
                                                                                            .active(true)
                                                                                            .createdAt(LocalDateTime.now())
                                                                                            .updatedAt(LocalDateTime.now())
                                                                                            .build())
                                                                            .flatMap(savedMember -> projectPermissionService.saveGroupMemberPermissions(
                                                                                            projectMemberCreate.getPermissions(), savedMember.getId()
                                                                                    )
                                                                                    .flatMap(savedPerms -> mapToResponse(savedMember, savedPerms)));
                                                                })
                                                );
                                    }

                                    return userRepository.findById(projectMemberCreate.getMember())
                                            .switchIfEmpty(Mono.error(new RuntimeException("No user with this ID exists")))
                                            .flatMap(user -> projectUserMemberRepository.findByProjectIdAndUserId(
                                                                    project.getId(), user.getId()
                                                            )
                                                            .hasElement()
                                                            .flatMap(exists -> {
                                                                if (exists) {
                                                                    return Mono.error(new RuntimeException("User is already a member of this project"));
                                                                }

                                                                return projectUserMemberRepository.save(
                                                                                ProjectUserMember.builder()
                                                                                        .projectId(project.getId())
                                                                                        .userId(user.getId())
                                                                                        .active(true)
                                                                                        .createdAt(LocalDateTime.now())
                                                                                        .updatedAt(LocalDateTime.now())
                                                                                        .build())
                                                                        .flatMap(savedMember -> projectPermissionService.saveUserMemberPermissions(
                                                                                        projectMemberCreate.getPermissions(), savedMember.getId()
                                                                                )
                                                                                .flatMap(savedPerms -> mapToResponse(savedMember, savedPerms)));
                                                            })
                                            );
                                })))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<ProjectMemberResponse> getProjectMembers(UUID projectId) {

        Flux<ProjectMemberResponse> userMembers = projectUserMemberRepository.findByProjectId(projectId)
                .flatMap(this::mapToResponse);

        Flux<ProjectMemberResponse> groupMembers = projectGroupMemberRepository.findByProjectId(projectId)
                .flatMap(this::mapToResponse);

        return Flux.merge(userMembers, groupMembers);
    }

    @Override
    public Mono<ProjectMemberResponse> getProjectMember(UUID projectId, UUID memberId) {
        return projectGroupMemberRepository
                .findByProjectIdAndGroupId(projectId, memberId)
                .flatMap(this::mapToResponse)
                .switchIfEmpty(
                        projectUserMemberRepository
                                .findByProjectIdAndUserId(projectId, memberId)
                                .flatMap(this::mapToResponse)
                )
                .switchIfEmpty(
                        Mono.error(new RuntimeException("Project member not found"))
                );
    }

    @Override
    public Mono<ProjectMemberResponse> removeMember(UUID projectMemberId) {

        return userRoleUtil.isAdmin()
                .flatMap(auth -> {

                    Mono<ProjectMemberResponse> removeUserMember =
                            projectUserMemberRepository.findById(projectMemberId)
                                    .flatMap(member ->
                                            projectRepository.findById(member.getProjectId())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                                                    .flatMap(project -> permissionResolver.hasPermission("REMOVE_MEMBER", auth.getUserId(), project.getId())
                                                            .flatMap(hasPermission -> {
                                                                if (!auth.getIsAdmin()
                                                                        && !project.getOwner().equals(auth.getUserId())
                                                                        && !hasPermission) {
                                                                    return Mono.error(new RuntimeException("You are not allowed to remove this member"));
                                                                }

                                                                return projectUserMemberRepository
                                                                        .deleteById(projectMemberId)
                                                                        .then(mapToResponse(member));
                                                            }))
                                    );

                    Mono<ProjectMemberResponse> removeGroupMember =
                            projectGroupMemberRepository.findById(projectMemberId)
                                    .flatMap(member ->
                                            projectRepository.findById(member.getProjectId())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                                                    .flatMap(project -> {
                                                        if (!auth.getIsAdmin()
                                                                && !project.getOwner().equals(auth.getUserId())) {
                                                            return Mono.error(new RuntimeException("You are not allowed to remove this member"));
                                                        }

                                                        return projectGroupMemberRepository
                                                                .deleteById(projectMemberId)
                                                                .then(mapToResponse(member));
                                                    })
                                    );

                    return removeUserMember
                            .switchIfEmpty(removeGroupMember)
                            .switchIfEmpty(Mono.error(new RuntimeException("Project member not found")));
                })
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Boolean> isMember(UUID projectId, UUID userId) {

        Mono<Boolean> directMember =
                projectUserMemberRepository
                        .findByProjectIdAndUserId(projectId, userId)
                        .filter(ProjectUserMember::getActive)
                        .hasElement();

        Mono<Boolean> groupMember = projectGroupMemberRepository
                .findByProjectId(projectId)
                .filter(ProjectGroupMember::getActive)
                .flatMap(projectGroupMember -> groupMemberRepository.findByGroupIdAndMemberId(projectGroupMember.getGroupId(), userId))
                .hasElements();

        return Mono.zip(directMember, groupMember)
                .map(tuple -> tuple.getT1() || tuple.getT2());
    }

    @Override
    public Flux<UserListResponse> getMemberUsers(UUID projectId) {
        return projectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                .flatMapMany(project -> {
                    Flux<UserResponse> directUser = projectUserMemberRepository.findByProjectId(projectId)
                            .filter(ProjectUserMember::getActive)
                            .flatMap(projectUserMember -> userService.getUserById(projectUserMember.getUserId()));


                    Flux<UserResponse> groupUser = projectGroupMemberRepository.findByProjectId(projectId)
                            .filter(ProjectGroupMember::getActive)
                            .flatMap(projectGroupMember -> groupRepository.findById(projectGroupMember.getGroupId())
                                    .flatMapMany(group -> groupMemberRepository.findAllByGroup(group.getId())
                                            .flatMap(groupMember -> userService.getUserById(groupMember.getMember()))
                                    )
                            );

                    return Flux.merge(directUser, groupUser)
                            .distinct()
                            .map(userResponse -> UserListResponse.builder()
                                    .id(userResponse.getId())
                                    .username(userResponse.getUsername())
                                    .firstName(userResponse.getFirstName())
                                    .lastName(userResponse.getLastName())
                                    .build());
                });
    }

    @Override
    public Flux<ProjectWisePermission> getProjectWisePermission(Group group, Boolean isAdmin) {
        return projectGroupMemberRepository.findByGroupId(group.getId())
                .flatMap(projectGroupMember -> projectRepository.findById(projectGroupMember.getProjectId())
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                        .flatMapMany(project -> groupMemberPermissionRepository.findAllByProjectGroupMemberIdAndActiveTrue(projectGroupMember.getId())
                                .flatMap(groupMemberPermission -> projectPermissionRepository.findById(groupMemberPermission.getPermissionId()))
                                .collectList()
                                .flatMapMany(projectPermissions -> groupMemberRepository.findAllByGroup(group.getId())
                                        .flatMap(groupMember -> userService.getUserById(groupMember.getMember())
                                                .flatMap(userResponse -> projectPermissionService.getDelegatedPermissionByProject(userResponse.getId(), project.getId())
                                                        .collectList()
                                                        .map(permissionResponses -> UserPermission.builder()
                                                                .id(userResponse.getId())
                                                                .username(userResponse.getUsername())
                                                                .firstName(userResponse.getFirstName())
                                                                .lastName(userResponse.getLastName())
                                                                .permissions(permissionResponses)
                                                                .build()
                                                        )
                                                )
                                        )
                                        .collectList()
                                        .flatMapMany(userPermissions -> Flux.just(ProjectWisePermission.builder()
                                                        .id(project.getId())
                                                        .name(project.getName())
                                                        .projectCode(project.getProjectCode())
                                                        .status(project.getStatus())
                                                        .owner(project.getOwner())
                                                        .permissions(projectPermissions)
                                                        .userPermissions(userPermissions)
                                                        .build()
                                                )
                                        )
                                )
                        )
                );
    }

    //HELPER METHODS
    private Mono<ProjectMemberResponse> mapToResponse(ProjectGroupMember projectGroupMember) {
        return Mono.zip(
                        projectRepository.findById(projectGroupMember.getProjectId()),
                        groupRepository.findById(projectGroupMember.getGroupId()),
                        projectPermissionService.getGroupMemberPermissions(projectGroupMember.getId()).collectList()
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectGroupMember.getId())
                                .project(tuple.getT1())
                                .group(tuple.getT2())
                                .member(projectGroupMember.getGroupId())
                                .permission(tuple.getT3())
                                .active(projectGroupMember.getActive())
                                .createdAt(projectGroupMember.getCreatedAt())
                                .updatedAt(projectGroupMember.getUpdatedAt())
                                .build());
    }

    private Mono<ProjectMemberResponse> mapToResponse(ProjectGroupMember projectGroupMember, List<PermissionResponse> permissions) {
        return Mono.zip(
                        projectRepository.findById(projectGroupMember.getProjectId()),
                        groupRepository.findById(projectGroupMember.getGroupId())
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectGroupMember.getId())
                                .project(tuple.getT1())
                                .group(tuple.getT2())
                                .member(projectGroupMember.getGroupId())
                                .active(projectGroupMember.getActive())
                                .permission(permissions)
                                .createdAt(projectGroupMember.getCreatedAt())
                                .updatedAt(projectGroupMember.getUpdatedAt())
                                .build());
    }

    private Mono<ProjectMemberResponse> mapToResponse(ProjectUserMember projectUserMember) {
        return Mono.zip(
                        projectRepository.findById(projectUserMember.getProjectId()),
                        userService.getUserById(projectUserMember.getUserId()),
                        projectPermissionService.getUserMemberPermissions(projectUserMember.getId()).collectList()
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectUserMember.getId())
                                .project(tuple.getT1())
                                .user(tuple.getT2())
                                .member(projectUserMember.getUserId())
                                .permission(tuple.getT3())
                                .active(projectUserMember.getActive())
                                .createdAt(projectUserMember.getCreatedAt())
                                .updatedAt(projectUserMember.getUpdatedAt())
                                .build());
    }

    private Mono<ProjectMemberResponse> mapToResponse(ProjectUserMember projectUserMember, List<PermissionResponse> permissions) {
        return Mono.zip(
                        projectRepository.findById(projectUserMember.getProjectId()),
                        userService.getUserById(projectUserMember.getUserId())
                )
                .map(tuple ->
                        ProjectMemberResponse.builder()
                                .id(projectUserMember.getId())
                                .project(tuple.getT1())
                                .user(tuple.getT2())
                                .member(projectUserMember.getUserId())
                                .active(projectUserMember.getActive())
                                .permission(permissions)
                                .createdAt(projectUserMember.getCreatedAt())
                                .updatedAt(projectUserMember.getUpdatedAt())
                                .build());
    }

}
