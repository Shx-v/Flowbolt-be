package com.shxv.authenticationTemplate.Project.Service;

import com.shxv.authenticationTemplate.Project.DTO.ProjectCreate;
import com.shxv.authenticationTemplate.Project.DTO.ProjectDetailResponse;
import com.shxv.authenticationTemplate.Project.DTO.ProjectResponse;
import com.shxv.authenticationTemplate.Project.DTO.ProjectUpdate;
import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectService {

    Mono<ProjectResponse> createProject(ProjectCreate projectCreate);
    Flux<ProjectResponse> getAllProjects();
    Mono<ProjectResponse> getProjectById(UUID id);
    Mono<ProjectDetailResponse> getProjectDetail(UUID id);
    Mono<ProjectResponse> getProjectByCode(String projectCode);
    Flux<ProjectResponse> getProjectsByOwner(UUID owner);
    Mono<ProjectResponse> updateProject(UUID id, ProjectUpdate projectUpdate);
    Mono<ProjectResponse> deleteProject(UUID id);
    Mono<ProjectResponse> archiveProject(UUID projectId);
    Mono<ProjectResponse> suspendProject(UUID projectId);
    Mono<ProjectResponse> restoreProject(UUID projectId);
    Mono<ProjectResponse> transferOwnership(UUID projectId, UUID newOwnerId);

//    Flux<ProjectResponse> getProjectsForCurrentUser();
//
//    Mono<Boolean> existsByProjectCode(String projectCode);
//    Mono<Boolean> existsById(UUID projectId);
//
//    Mono<Boolean> canViewProject(UUID projectId);
//    Mono<Boolean> canEditProject(UUID projectId);
//    Mono<Boolean> canDeleteProject(UUID projectId);

}

