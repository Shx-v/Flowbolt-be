package com.shxv.authenticationTemplate.ProjectMember.Service;

import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Group.Repository.GroupRepository;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.DTO.DelegatePermissionRequest;
import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionResponse;
import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionUpdate;
import com.shxv.authenticationTemplate.ProjectMember.Model.GroupMemberPermission;
import com.shxv.authenticationTemplate.ProjectMember.Model.GroupPermissionDelegation;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectPermission;
import com.shxv.authenticationTemplate.ProjectMember.Model.UserMemberPermission;
import com.shxv.authenticationTemplate.ProjectMember.Repository.*;
import com.shxv.authenticationTemplate.ProjectMember.Util.PermissionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectPermissionServiceImpl implements ProjectPermissionService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    ProjectPermissionRepository projectPermissionRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserMemberPermissionRepository userMemberPermissionRepository;

    @Autowired
    GroupMemberPermissionRepository groupMemberPermissionRepository;

    @Autowired
    ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    ProjectGroupMemberRepository projectGroupMemberRepository;

    @Autowired
    GroupPermissionDelegationRepository groupPermissionDelegationRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    PermissionResolver permissionResolver;

    @Override
    public Flux<ProjectPermission> getAllPermission() {
        return projectPermissionRepository.findAll();
    }

    @Override
    public Mono<List<PermissionResponse>> saveUserMemberPermissions(List<UUID> permissions, UUID projectMemberId) {
        if (permissions == null || permissions.isEmpty()) {
            return Mono.just(List.of());
        }

        return Flux.fromIterable(permissions)
                .flatMap(permissionId ->
                        projectPermissionRepository.findById(permissionId)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("Invalid permission: " + permissionId)
                                ))
                                .flatMap(permission ->
                                        userMemberPermissionRepository.findByProjectUserMemberIdAndPermissionId(projectMemberId, permissionId)
                                                .hasElement()
                                                .flatMap(exists -> {
                                                    if (exists) {
                                                        return Mono.empty();
                                                    }

                                                    return userMemberPermissionRepository.save(
                                                            UserMemberPermission.builder()
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
                .flatMap(this::mapToResponse)
                .collectList();
    }

    @Override
    public Mono<List<PermissionResponse>> saveGroupMemberPermissions(List<UUID> permissions, UUID projectMemberId) {
        if (permissions == null || permissions.isEmpty()) {
            return Mono.just(List.of());
        }

        return Flux.fromIterable(permissions)
                .flatMap(permissionId ->
                        projectPermissionRepository.findById(permissionId)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("Invalid permission: " + permissionId)
                                ))
                                .flatMap(permission ->
                                        groupMemberPermissionRepository.findByProjectGroupMemberIdAndPermissionId(projectMemberId, permissionId)
                                                .hasElement()
                                                .flatMap(exists -> {
                                                    if (exists) {
                                                        return Mono.empty();
                                                    }

                                                    return groupMemberPermissionRepository.save(
                                                            GroupMemberPermission.builder()
                                                                    .projectGroupMemberId(projectMemberId)
                                                                    .permissionId(permissionId)
                                                                    .active(true)
                                                                    .build()
                                                    );
                                                })
                                )
                )
                .flatMap(this::mapToResponse)
                .collectList();
    }

    @Override
    public Flux<PermissionResponse> getUserMemberPermissions(UUID projectMemberId) {
        return userMemberPermissionRepository
                .findAllByProjectUserMemberIdAndActiveTrue(projectMemberId)
                .flatMap(this::mapToResponse);
    }

    @Override
    public Flux<PermissionResponse> getGroupMemberPermissions(UUID projectMemberId) {
        return groupMemberPermissionRepository
                .findAllByProjectGroupMemberIdAndActiveTrue(projectMemberId)
                .flatMap(this::mapToResponse);
    }

    @Override
    public Mono<List<PermissionResponse>> updateMemberPermissions(UUID projectMemberId, PermissionUpdate permissionUpdate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectUserMemberRepository.findById(projectMemberId)
                        .flatMap(userMember -> projectRepository.findById(userMember.getProjectId())
                                .flatMap(project -> permissionResolver.hasPermission("UPDATE_PERMISSION", adminCheckResponse.getUserId(), project.getId())
                                        .flatMap(hasPermission -> {
                                            if (!adminCheckResponse.getIsAdmin() && !project.getOwner().equals(adminCheckResponse.getUserId()) && !hasPermission) {
                                                return Mono.error(new RuntimeException("You are not allowed to update member permissions"));
                                            }

                                            return updateUserMemberPermissions(projectMemberId, permissionUpdate.getPermissions());
                                        })
                                )
                        )
                        .switchIfEmpty(projectGroupMemberRepository.findById(projectMemberId)
                                .flatMap(groupMember -> projectRepository.findById(groupMember.getProjectId())
                                        .flatMap(project -> permissionResolver.hasPermission("UPDATE_PERMISSION", adminCheckResponse.getUserId(), project.getId())
                                                .flatMap(hasPermission -> {
                                                    if (!adminCheckResponse.getIsAdmin() && !project.getOwner().equals(adminCheckResponse.getUserId()) && !hasPermission) {
                                                        return Mono.error(new RuntimeException("You are not allowed to update member permissions"));
                                                    }

                                                    return updateGroupMemberPermissions(projectMemberId, permissionUpdate.getPermissions());
                                                })
                                        )
                                )
                        )
                );
    }

    @Override
    public Mono<List<PermissionResponse>> setDelegatedPermissions(DelegatePermissionRequest request) {
        List<UUID> permissionIds = request.getPermissions() == null ? List.of() : request.getPermissions();

        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> groupRepository.findById(request.getGroup())
                        .switchIfEmpty(Mono.error(new RuntimeException("No group with this ID exists")))
                        .flatMap(group -> {
                            if (!group.getLeader().equals(currentUserId)) {
                                return Mono.error(new RuntimeException("You are not allowed to set the permission"));
                            }

                            return projectRepository.findById(request.getProject())
                                    .switchIfEmpty(Mono.error(new RuntimeException("No project with this ID exists")))
                                    .flatMap(project -> groupMemberRepository.findByGroupIdAndMemberId(group.getId(), request.getDelegatedToUser())
                                            .switchIfEmpty(Mono.error(new RuntimeException("Delegated user is not a member of this group")))
                                            .flatMap(groupMember -> projectGroupMemberRepository.findByProjectIdAndGroupId(request.getProject(), group.getId())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("This group is not a member of this project")))
                                                    .flatMap(projectGroupMember -> groupMemberPermissionRepository.findAllByProjectGroupMemberIdAndActiveTrue(projectGroupMember.getId())
                                                            .collectList()
                                                            .flatMap(groupMemberPermissions -> projectPermissionRepository
                                                                    .findAllById(permissionIds)
                                                                    .collectList()
                                                                    .flatMap(existingPermissions -> {
                                                                        if (existingPermissions.size() != permissionIds.size()) {
                                                                            return Mono.error(new RuntimeException("One or more permission IDs are invalid"));
                                                                        }

                                                                        Set<UUID> groupPermissionIds = groupMemberPermissions.stream()
                                                                                .map(GroupMemberPermission::getPermissionId)
                                                                                .collect(Collectors.toSet());

                                                                        if (!groupPermissionIds.containsAll(permissionIds)) {
                                                                            return Mono.error(new RuntimeException("One or more permissions are not assigned to this group"));
                                                                        }

                                                                        Map<UUID, UUID> permissionToGmpIdMap = groupMemberPermissions.stream()
                                                                                .collect(Collectors.toMap(
                                                                                        GroupMemberPermission::getPermissionId,
                                                                                        GroupMemberPermission::getId
                                                                                ));

                                                                        Set<UUID> allGroupGmpIds = new HashSet<>(permissionToGmpIdMap.values());

                                                                        Set<UUID> targetGmpIds = permissionIds.stream()
                                                                                .map(permissionToGmpIdMap::get)
                                                                                .collect(Collectors.toSet());

                                                                        Mono<Void> deleteOld = groupPermissionDelegationRepository
                                                                                .findByDelegatedToUserId(request.getDelegatedToUser())
                                                                                .filter(d -> allGroupGmpIds.contains(d.getProjectGroupMemberPermissionId()))
                                                                                .flatMap(groupPermissionDelegationRepository::delete)
                                                                                .then();

                                                                        Flux<GroupPermissionDelegation> insertNew = Flux.fromIterable(targetGmpIds)
                                                                                .map(gmpId -> GroupPermissionDelegation.builder()
                                                                                        .delegatedToUserId(request.getDelegatedToUser())
                                                                                        .projectGroupMemberPermissionId(gmpId)
                                                                                        .active(true)
                                                                                        .createdAt(LocalDateTime.now())
                                                                                        .updatedAt(LocalDateTime.now())
                                                                                        .build()
                                                                                )
                                                                                .flatMap(groupPermissionDelegationRepository::save);

                                                                        return deleteOld
                                                                                .thenMany(insertNew)
                                                                                .thenMany(Flux.fromIterable(permissionIds).flatMap(pid ->
                                                                                                projectPermissionRepository.findById(pid)
                                                                                                        .map(permission -> PermissionResponse.builder()
                                                                                                                .key(permission.getKey())
                                                                                                                .description(permission.getDescription())
                                                                                                                .build()
                                                                                                        )
                                                                                        )
                                                                                )
                                                                                .collectList();
                                                                    })
                                                            )
                                                    )
                                            )
                                    );
                        })
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<PermissionResponse> getDelegatedPermissions(UUID delegatedToUserId) {
        return groupPermissionDelegationRepository
                .findByDelegatedToUserId(delegatedToUserId)
                .flatMap(delegation ->
                        groupMemberPermissionRepository
                                .findById(delegation.getProjectGroupMemberPermissionId())
                                .flatMap(groupMemberPermission ->
                                        projectPermissionRepository
                                                .findById(groupMemberPermission.getPermissionId())
                                )
                                .map(projectPermission -> PermissionResponse.builder()
                                        .key(projectPermission.getKey())
                                        .description(projectPermission.getDescription())
                                        .build())
                );
    }

    @Override
    public Flux<ProjectPermission> getDelegatedPermissionByProject(
            UUID delegatedToUserId,
            UUID projectId
    ) {
        return groupPermissionDelegationRepository
                .findByDelegatedToUserId(delegatedToUserId)
                .flatMap(delegation ->
                        groupMemberPermissionRepository
                                .findById(delegation.getProjectGroupMemberPermissionId())
                )
                .flatMap(groupMemberPermission ->
                        projectGroupMemberRepository
                                .findById(groupMemberPermission.getProjectGroupMemberId())
                                .filter(projectGroupMember ->
                                        projectGroupMember.getProjectId().equals(projectId)
                                )
                                .map(projectGroupMember -> groupMemberPermission)
                )
                .flatMap(groupMemberPermission ->
                        projectPermissionRepository
                                .findById(groupMemberPermission.getPermissionId())
                )
                .filter(ProjectPermission::getActive);
    }


    @Override
    public Mono<List<PermissionResponse>> revokeDelegatedPermission(UUID delegationId) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> groupPermissionDelegationRepository
                        .findById(delegationId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Delegated permission not found")))
                        .flatMap(delegation -> groupMemberPermissionRepository
                                .findById(delegation.getProjectGroupMemberPermissionId())
                                .switchIfEmpty(Mono.error(new RuntimeException("Group member permission not found")))
                                .flatMap(groupMemberPermission -> projectGroupMemberRepository
                                        .findById(groupMemberPermission.getProjectGroupMemberId())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Group member not found")))
                                        .flatMap(groupMember -> groupRepository
                                                .findById(groupMember.getGroupId())
                                                .switchIfEmpty(Mono.error(new RuntimeException("Group not found")))
                                                .flatMap(group -> {
                                                    if (!group.getLeader().equals(currentUserId)) {
                                                        return Mono.error(new RuntimeException("Only group leader can revoke delegated permissions"));
                                                    }
                                                    return Mono.just(groupMemberPermission);
                                                })
                                        )
                                )
                                .flatMap(groupMemberPermission -> projectPermissionRepository
                                        .findById(groupMemberPermission.getPermissionId())
                                        .switchIfEmpty(Mono.error(new RuntimeException("Permission not found")))
                                        .flatMap(permission -> groupPermissionDelegationRepository
                                                .deleteById(delegationId)
                                                .thenReturn(List.of(
                                                        PermissionResponse.builder()
                                                                .key(permission.getKey())
                                                                .description(permission.getDescription())
                                                                .build()
                                                ))
                                        )
                                )
                        )
                        .as(transactionalOperator::transactional));
    }

    //HELPER METHODS
    private Mono<PermissionResponse> mapToResponse(UserMemberPermission userMemberPermission) {
        return projectPermissionRepository.findById(userMemberPermission.getPermissionId())
                .map(permission -> PermissionResponse.builder()
                        .key(permission.getKey())
                        .description(permission.getDescription())
                        .build()
                );
    }

    private Mono<PermissionResponse> mapToResponse(GroupMemberPermission groupMemberPermission) {
        return projectPermissionRepository.findById(groupMemberPermission.getPermissionId())
                .map(permission -> PermissionResponse.builder()
                        .key(permission.getKey())
                        .description(permission.getDescription())
                        .build()
                );
    }

    private Mono<List<PermissionResponse>> updateUserMemberPermissions(UUID projectUserMemberId, List<UUID> permissions) {
        return userMemberPermissionRepository.deleteByProjectUserMemberId(projectUserMemberId)
                .thenMany(Flux.fromIterable(permissions))
                .flatMap(permissionId ->
                        userMemberPermissionRepository.save(
                                UserMemberPermission.builder()
                                        .projectUserMemberId(projectUserMemberId)
                                        .permissionId(permissionId)
                                        .active(true)
                                        .build()
                        )
                )
                .flatMap(this::mapToResponse)
                .collectList();
    }

    private Mono<List<PermissionResponse>> updateGroupMemberPermissions(UUID projectGroupMemberId, List<UUID> permissions) {
        return groupMemberPermissionRepository
                .deleteByProjectGroupMemberId(projectGroupMemberId)
                .thenMany(Flux.fromIterable(permissions))
                .flatMap(permissionId ->
                        groupMemberPermissionRepository.save(
                                GroupMemberPermission.builder()
                                        .projectGroupMemberId(projectGroupMemberId)
                                        .permissionId(permissionId)
                                        .active(true)
                                        .build()
                        )
                )
                .flatMap(this::mapToResponse)
                .collectList();
    }

}
