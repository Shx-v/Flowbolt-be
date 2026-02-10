package com.shxv.authenticationTemplate.ProjectMember.Util;

import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class PermissionResolver {

    @Autowired
    ProjectPermissionRepository projectPermissionRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    UserMemberPermissionRepository userMemberPermissionRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

    @Autowired
    ProjectGroupMemberRepository projectGroupMemberRepository;

    @Autowired
    GroupMemberPermissionRepository groupMemberPermissionRepository;

    @Autowired
    GroupPermissionDelegationRepository groupPermissionDelegationRepository;

    public Mono<Boolean> hasPermission(String permission, UUID userId, UUID projectId) {

        return projectPermissionRepository.findByKey(permission)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid permission key")))
                .flatMap(projectPermission -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                        .flatMap(project -> projectUserMemberRepository
                                .findByProjectIdAndUserId(project.getId(), userId)
                                .flatMap(projectUserMember -> userMemberPermissionRepository
                                        .findByProjectUserMemberIdAndPermissionId(projectUserMember.getId(), projectPermission.getId())
                                        .hasElement()
                                        .filter(Boolean::booleanValue)
                                )
                                .switchIfEmpty(groupMemberRepository.findAllByMember(userId)
                                        .flatMap(groupMember -> projectGroupMemberRepository
                                                .findByProjectIdAndGroupId(project.getId(), groupMember.getGroup())
                                                .flatMap(projectGroupMember -> groupMemberPermissionRepository
                                                        .findByProjectGroupMemberIdAndPermissionId(projectGroupMember.getId(), projectPermission.getId())
                                                        .flatMap(groupMemberPermission -> groupPermissionDelegationRepository
                                                                .findByDelegatedToUserIdAndProjectGroupMemberPermissionId(userId, groupMemberPermission.getId())
                                                        )
                                                )
                                        )
                                        .hasElements()
                                )
                        )
                )
                .defaultIfEmpty(false);
    }

}
