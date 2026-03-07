package com.shxv.flowbolt.ProjectMember.Util;

import com.shxv.flowbolt.Project.Repository.ProjectRepository;
import com.shxv.flowbolt.ProjectMember.Repository.*;
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
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    MemberPermissionRepository memberPermissionRepository;

    public Mono<Boolean> hasPermission(String permission, UUID userId, UUID projectId) {

        return projectPermissionRepository.findByKey(permission)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid permission key")))
                .flatMap(projectPermission -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                        .flatMap(project -> projectMemberRepository
                                .findByProjectIdAndUserId(project.getId(), userId)
                                .flatMap(projectUserMember -> memberPermissionRepository
                                        .findByProjectUserMemberIdAndPermissionId(projectUserMember.getId(), projectPermission.getId())
                                        .hasElement()
                                        .filter(Boolean::booleanValue)
                                )
                        )
                )
                .defaultIfEmpty(false);
    }

}
