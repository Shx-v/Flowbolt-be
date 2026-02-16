package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.ProjectMemberResponseDTO;
import com.shxv.authenticationTemplate.Model.ProjectMember;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectMemberService {

    Mono<ProjectMemberResponseDTO> addMember(UUID projectId, UUID memberId);

    Mono<Void> removeMember(UUID projectId, UUID memberId);

    Flux<ProjectMemberResponseDTO> getProjectMembers(UUID projectId);

    Mono<Boolean> isMember(UUID projectId, UUID memberId);

    Flux<ProjectMember> getProjectsByUser(UUID userId);
}
