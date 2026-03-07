package com.shxv.flowbolt.Project.Service;

import com.shxv.flowbolt.Dashboard.DTO.ProjectHealthMetrics;
import com.shxv.flowbolt.Project.DTO.ProjectCreate;
import com.shxv.flowbolt.Project.DTO.ProjectDetailResponse;
import com.shxv.flowbolt.Project.DTO.ProjectResponse;
import com.shxv.flowbolt.Project.DTO.ProjectUpdate;
import com.shxv.flowbolt.Project.Enum.ProjectStatus;
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
    Mono<ProjectResponse> archiveProject(UUID projectId);
    Mono<ProjectResponse> suspendProject(UUID projectId);
    Mono<ProjectResponse> restoreProject(UUID projectId);
    Mono<ProjectResponse> transferOwnership(UUID projectId, UUID newOwnerId);
    Mono<Void> deleteProjectById(UUID uuid);
    Mono<Long> getTotalProjects();
    Mono<Long> getActiveProjects();
    Flux<ProjectHealthMetrics> getProjectHealthMatrics();

}

