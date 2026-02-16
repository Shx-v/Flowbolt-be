package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.ProjectMemberResponseDTO;
import com.shxv.authenticationTemplate.Model.ProjectMember;
import com.shxv.authenticationTemplate.Repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Override
    public Mono<ProjectMemberResponseDTO> addMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.findByProjectAndMember(projectId, memberId)
                .flatMap(existing ->
                        Mono.error(new RuntimeException("Member already added to this project"))
                )
                .switchIfEmpty(
                        projectMemberRepository.save(
                                ProjectMember.builder()
                                        .project(projectId)
                                        .member(memberId)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        )
                )
                .cast(ProjectMember.class)
                .map(pm -> ProjectMemberResponseDTO.builder()
                        .id(pm.getId())
                        .project(pm.getProject())
                        .member(pm.getMember())
                        .createdAt(pm.getCreatedAt())
                        .updatedAt(pm.getUpdatedAt())
                        .build());
    }

    @Override
    public Mono<Void> removeMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.findByProjectAndMember(projectId, memberId)
                .switchIfEmpty(Mono.error(new RuntimeException("Member not found in the project")))
                .flatMap(projectMemberRepository::delete);
    }

    @Override
    public Flux<ProjectMemberResponseDTO> getProjectMembers(UUID projectId) {
        return projectMemberRepository.findAllByProject(projectId)
                .map(pm -> ProjectMemberResponseDTO.builder()
                        .id(pm.getId())
                        .project(pm.getProject())
                        .member(pm.getMember())
                        .createdAt(pm.getCreatedAt())
                        .updatedAt(pm.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public Mono<Boolean> isMember(UUID projectId, UUID memberId) {
        return projectMemberRepository.findByProjectAndMember(projectId, memberId)
                .map(pm -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<ProjectMember> getProjectsByUser(UUID userId) {
        return projectMemberRepository.findAllByMember(userId);
    }

}
