package com.shxv.authenticationTemplate.Group.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Group.DTO.GroupMemberCreate;
import com.shxv.authenticationTemplate.Group.DTO.GroupMemberResponse;
import com.shxv.authenticationTemplate.Group.Enum.GroupStatus;
import com.shxv.authenticationTemplate.Group.Model.GroupMember;
import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Group.Repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    UserService userService;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    GroupRepository groupRepository;

    @Override
    public Mono<GroupMemberResponse> addMember(GroupMemberCreate groupMemberCreate, UUID groupId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> groupRepository.findById(groupId)
                        .switchIfEmpty(Mono.error(new RuntimeException("No group with this id exists")))
                        .flatMap(group -> {
                            if (!group.getLeader().equals(adminCheckResponse.getUserId()) && !adminCheckResponse.getIsAdmin()) {
                                return Mono.error(new RuntimeException("You are not allowed to add member(s) in this group"));
                            }

                            if(!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This group is not active"));
                            }

                            return groupMemberRepository.findByGroupIdAndMemberId(groupId, groupMemberCreate.getMember())
                                    .hasElement()
                                    .flatMap(exists -> {
                                                if (exists) {
                                                    return Mono.error(
                                                            new RuntimeException("User is already a member of this group")
                                                    );
                                                }

                                                return groupMemberRepository.save(
                                                        GroupMember.builder()
                                                                .group(groupId)
                                                                .member(groupMemberCreate.getMember())
                                                                .createdBy(adminCheckResponse.getUserId())
                                                                .createdAt(LocalDateTime.now())
                                                                .updatedAt(LocalDateTime.now())
                                                                .build()
                                                ).flatMap(this::mapToResponse);

                                            }
                                    );
                        }));
    }

    @Override
    public Mono<List<UserListResponse>> addAllMember(List<GroupMemberCreate> groupMemberCreateList, UUID leaderId, UUID groupId) {
        List<GroupMember> membersToSave = groupMemberCreateList.stream()
                .map(dto -> GroupMember.builder()
                        .group(groupId)
                        .member(dto.getMember())
                        .createdBy(leaderId)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();

        return groupMemberRepository.saveAll(membersToSave)
                .flatMap(savedMember -> userService.getUserById(savedMember.getMember())
                        .map(userResponse -> UserListResponse.builder()
                                .id(userResponse.getId())
                                .firstName(userResponse.getFirstName())
                                .lastName(userResponse.getLastName())
                                .username(userResponse.getUsername())
                                .build()))
                .collectList();
    }

    @Override
    public Flux<UserListResponse> getAllMemberByGroupId(UUID groupId) {
        return groupMemberRepository.findAllByGroup(groupId)
                .flatMap(groupMember -> userService.getUserById(groupMember.getMember())
                        .map(userResponse -> UserListResponse.builder()
                                .id(userResponse.getId())
                                .firstName(userResponse.getFirstName())
                                .lastName(userResponse.getLastName())
                                .username(userResponse.getUsername())
                                .build()));
    }

    @Override
    public Mono<GroupMemberResponse> removeMember(UUID groupId, UUID memberId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse ->
                        groupRepository.findById(groupId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Group with this ID does not exist")))
                                .flatMap(group -> {

                                    if(!group.getStatus().equals(GroupStatus.ACTIVE)) {
                                        return Mono.error(new RuntimeException("This group is not active"));
                                    }

                                    if (group.getLeader().equals(memberId)) {
                                        return Mono.error(new RuntimeException("Group leader cannot be removed"));
                                    }

                                    if (!group.getLeader().equals(adminCheckResponse.getUserId())
                                            && !adminCheckResponse.getIsAdmin()) {
                                        return Mono.error(new RuntimeException("You are not allowed to remove members from this group"));
                                    }

                                    return groupMemberRepository.findByGroupIdAndMemberId(groupId, memberId)
                                            .switchIfEmpty(Mono.error(new RuntimeException("This user is not a member of the group")))
                                            .flatMap(groupMember ->
                                                    groupMemberRepository.deleteByGroupIdAndMemberId(groupId, memberId)
                                                            .then(mapToResponse(groupMember))
                                            );
                                })
                );
    }

    //HELPER METHODS
    private Mono<GroupMemberResponse> mapToResponse(GroupMember groupMember) {
        return Mono.zip(
                        userService.getUserById(groupMember.getMember()),
                        userService.getUserById(groupMember.getCreatedBy()),
                        groupRepository.findById(groupMember.getGroup())
                )
                .map(tuple -> GroupMemberResponse.builder()
                        .id(groupMember.getId())
                        .groupId(tuple.getT3().getId())
                        .groupName(tuple.getT3().getName())
                        .member(tuple.getT1())
                        .createdBy(tuple.getT2())
                        .createdAt(groupMember.getCreatedAt())
                        .updatedAt(groupMember.getUpdatedAt())
                        .build());
    }
}
