package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.ProjectCreateRequestDTO;
import com.shxv.authenticationTemplate.DTO.ProjectResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectService {
    Flux<ProjectResponseDTO> getAllProjects();

    Mono<ProjectResponseDTO> getProjectById(UUID uuid);

    Mono<ProjectResponseDTO> createProject(ProjectCreateRequestDTO projectCreateRequestDTO);

    Mono<ProjectResponseDTO> updateProject(UUID uuid, ProjectCreateRequestDTO projectCreateRequestDTO);

    Mono<ProjectResponseDTO> deleteProject(UUID uuid);
}
