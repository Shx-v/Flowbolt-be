package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.ProjectCountResponseDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectCountService {

    Mono<ProjectCountResponseDTO> createProjectCount(UUID projectId);

    Mono<ProjectCountResponseDTO> getProjectCount(UUID projectId);

    Mono<Integer> incrementCount(UUID projectId);

    Mono<ProjectCountResponseDTO> decrementCount(UUID projectId);

    Mono<ProjectCountResponseDTO> setCount(UUID projectId, Integer newCount);

    Mono<Void> deleteProjectCount(UUID projectId);
}
