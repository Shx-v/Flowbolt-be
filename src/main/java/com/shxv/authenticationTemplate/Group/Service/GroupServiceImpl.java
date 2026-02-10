package com.shxv.authenticationTemplate.Group.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Group.DTO.*;
import com.shxv.authenticationTemplate.Group.Enum.GroupStatus;
import com.shxv.authenticationTemplate.Group.Model.Group;
import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Group.Repository.GroupRepository;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.GroupMemberPermissionRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectGroupMemberRepository;
import com.shxv.authenticationTemplate.ProjectMember.Service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    ProjectMemberService projectMemberService;

    @Override
    public Flux<GroupListResponse> getGroupList() {
        return groupRepository.findAll()
                .map(group -> GroupListResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .build()
                );
    }

    @Override
    public Mono<GroupResponse> createGroup(GroupCreate groupCreate) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> groupRepository.save(
                                Group.builder()
                                        .name(groupCreate.getName())
                                        .description(groupCreate.getDescription())
                                        .status(GroupStatus.ACTIVE)
                                        .leader(currentUserId)
                                        .createdBy(currentUserId)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build())
                        .flatMap(savedGroup -> {
                            List<GroupMemberCreate> toAdd = new ArrayList<>();

                            toAdd.add(
                                    GroupMemberCreate.builder()
                                            .member(currentUserId)
                                            .build()
                            );

                            groupCreate.getMembers().stream()
                                    .filter(memberId -> !memberId.equals(currentUserId))
                                    .forEach(memberId ->
                                            toAdd.add(
                                                    GroupMemberCreate.builder()
                                                            .member(memberId)
                                                            .build()
                                            )
                                    );

                            return groupMemberService.addAllMember(toAdd, currentUserId, savedGroup.getId())
                                    .flatMap(savedMembers -> mapToResponse(savedGroup, savedMembers));
                        }))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<GroupResponse> getGroupById(UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Group with this ID does not exist")))
                        .flatMap(group -> {
                            if (!adminCheckResponse.getIsAdmin() && !group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("Group with this ID does not exist"));
                            }

                            return mapToResponse(group);
                        }));
    }

    @Override
    public Mono<GroupDetailResponse> getGroupDetailsById(UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Group with this ID does not exist")))
                        .flatMap(group -> {
                            if (!adminCheckResponse.getIsAdmin() && !group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("Group with this ID does not exist"));
                            }

                            return projectMemberService.getProjectWisePermission(group, adminCheckResponse.getIsAdmin())
                                    .collectList()
                                    .flatMap(projectWisePermissions -> mapToDetailResponse(group, projectWisePermissions));
                        }));
    }

    @Override
    public Flux<GroupResponse> getAllGroups() {
        return userRoleUtil.isAdmin()
                .flatMapMany(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return groupRepository.findAll()
                                .flatMap(this::mapToResponse);
                    }

                    return groupRepository.findAllByStatus(GroupStatus.ACTIVE)
                            .flatMap(this::mapToResponse);
                });
    }

    @Override
    public Flux<GroupResponse> getMyGroups() {
        return userRoleUtil.isAdmin()
                .flatMapMany(adminCheckResponse ->
                        groupRepository.findMyGroups(adminCheckResponse.getUserId())
                                .flatMap(this::mapToResponse)
                );
    }

    @Override
    public Mono<GroupResponse> updateGroup(UUID groupId, GroupUpdate groupUpdate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Group with this ID does not exist")))
                        .flatMap(group -> {
                            if (!adminCheckResponse.getIsAdmin() && !group.getLeader().equals(adminCheckResponse.getUserId())) {
                                return Mono.error(new RuntimeException("You not allowed to do changes to this group"));
                            }

                            if (!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This group is not active"));
                            }

                            if (groupUpdate.getName() != null
                                    && !groupUpdate.getName().isBlank()) {
                                group.setName(groupUpdate.getName());
                            }

                            if (groupUpdate.getDescription() != null
                                    && !groupUpdate.getDescription().isBlank()) {
                                group.setDescription(groupUpdate.getDescription());
                            }

                            group.setUpdatedAt(LocalDateTime.now());

                            return groupRepository.save(group)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    @Override
    public Mono<GroupResponse> transferLeadership(UUID groupId, UUID memberId) {

        if (memberId == null) {
            return Mono.error(new RuntimeException("Member ID must not be null"));
        }

        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse ->
                        groupRepository.findById(groupId)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("Group with this ID does not exist")
                                ))
                                .flatMap(group -> {

                                    if (!adminCheckResponse.getIsAdmin()
                                            && !group.getLeader().equals(adminCheckResponse.getUserId())) {
                                        return Mono.error(
                                                new RuntimeException("You are not allowed to update this group")
                                        );
                                    }

                                    if (!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                        return Mono.error(new RuntimeException("This group is not active"));
                                    }

                                    if (group.getLeader().equals(memberId)) {
                                        return Mono.error(
                                                new RuntimeException("The user is already the leader of this group")
                                        );
                                    }

                                    return groupMemberRepository
                                            .findByGroupIdAndMemberId(groupId, memberId)
                                            .hasElement()
                                            .flatMap(isMember -> {
                                                if (!isMember) {
                                                    return Mono.error(
                                                            new RuntimeException("Leadership can be transferred only to a group member")
                                                    );
                                                }

                                                group.setLeader(memberId);
                                                group.setUpdatedAt(LocalDateTime.now());

                                                return groupRepository.save(group)
                                                        .flatMap(this::mapToResponse);
                                            });
                                })
                );
    }

    @Override
    public Mono<GroupResponse> leaveGroup(UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Group with this ID does not exist")))
                        .flatMap(group -> {

                            if (!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("You cannot leave an inactive group"));
                            }

                            UUID userId = adminCheckResponse.getUserId();

                            if (group.getLeader().equals(userId)) {
                                return Mono.error(
                                        new RuntimeException(
                                                "Group leader cannot leave the group. Transfer leadership first."
                                        )
                                );
                            }

                            return groupMemberRepository.findByGroupIdAndMemberId(groupId, userId)
                                    .switchIfEmpty(Mono.error(new RuntimeException("You are not a member of this group")))
                                    .flatMap(member -> groupMemberRepository
                                            .deleteByGroupIdAndMemberId(groupId, userId)
                                            .thenReturn(group)
                                    )
                                    .flatMap(this::mapToResponse);
                        })
                );
    }

    @Override
    public Mono<GroupResponse> archiveGroup(UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(
                                new RuntimeException("Group with this ID does not exist")
                        ))
                        .flatMap(group -> {
                            if (!group.getLeader().equals(adminCheckResponse.getUserId()) && !adminCheckResponse.getIsAdmin()) {
                                return Mono.error(
                                        new RuntimeException("You are not allowed to archive this group")
                                );
                            }

                            if (!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This group is not active"));
                            }

                            group.setStatus(GroupStatus.ARCHIVED);
                            group.setUpdatedAt(LocalDateTime.now());

                            return groupRepository.save(group)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    @Override
    public Mono<GroupResponse> restoreGroup(UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(
                                new RuntimeException("Group with this ID does not exist")
                        ))
                        .flatMap(group -> {
                            if (!adminCheckResponse.getIsAdmin()) {
                                return Mono.error(
                                        new RuntimeException("You are not allowed to restore this group")
                                );
                            }

                            if (group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This group is already active"));
                            }

                            group.setStatus(GroupStatus.ACTIVE);
                            group.setUpdatedAt(LocalDateTime.now());

                            return groupRepository.save(group)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    //HELPER METHODS
    private Mono<GroupResponse> mapToResponse(Group group, List<UserListResponse> members) {
        return Mono.just(GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .status(group.getStatus().getLabel())
                .leader(group.getLeader())
                .createdBy(group.getCreatedBy())
                .members(members)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build());
    }

    private Mono<GroupResponse> mapToResponse(Group group) {
        return groupMemberService.getAllMemberByGroupId(group.getId())
                .collectList()
                .flatMap(members -> mapToResponse(group, members));
    }

    private Mono<GroupDetailResponse> mapToDetailResponse(Group group, List<ProjectWisePermission> projects) {
        return groupMemberService.getAllMemberByGroupId(group.getId())
                .collectList()
                .flatMap(members -> Mono.just(GroupDetailResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .description(group.getDescription())
                        .status(group.getStatus().getLabel())
                        .leader(group.getLeader())
                        .createdBy(group.getCreatedBy())
                        .members(members)
                        .projects(projects)
                        .createdAt(group.getCreatedAt())
                        .updatedAt(group.getUpdatedAt())
                        .build())
                );
    }

}
