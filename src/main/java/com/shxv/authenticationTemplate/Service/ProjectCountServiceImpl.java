package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.ProjectCountResponseDTO;
import com.shxv.authenticationTemplate.Model.ProjectCount;
import com.shxv.authenticationTemplate.Repository.ProjectCountRepository;
import com.shxv.authenticationTemplate.Repository.ProjectCountRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ProjectCountServiceImpl implements ProjectCountService {

    @Autowired
    ProjectCountRepository projectCountRepository;

    @Autowired
    ProjectCountRepositoryCustom projectCountRepositoryCustom;

    @Override
    public Mono<ProjectCountResponseDTO> createProjectCount(UUID projectId) {
        return projectCountRepository.findByProject(projectId)
                .flatMap(existing ->
                        Mono.error(new RuntimeException("Project count already exists"))
                )
                .switchIfEmpty(
                        Mono.defer(() ->
                                projectCountRepository.save(
                                        ProjectCount.builder()
                                                .project(projectId)
                                                .ticketCount(0)
                                                .build()
                                )
                        )
                )
                .cast(ProjectCount.class)
                .map(pc -> ProjectCountResponseDTO.builder()
                        .id(pc.getId())
                        .project(pc.getProject())
                        .ticketCount(pc.getTicketCount())
                        .createdAt(pc.getCreatedAt())
                        .updatedAt(pc.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public Mono<ProjectCountResponseDTO> getProjectCount(UUID projectId) {
        return projectCountRepository.findByProject(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project count not found")))
                .map(pc -> ProjectCountResponseDTO.builder()
                        .id(pc.getId())
                        .project(pc.getProject())
                        .ticketCount(pc.getTicketCount())
                        .createdAt(pc.getCreatedAt())
                        .updatedAt(pc.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public Mono<Integer> incrementCount(UUID projectId) {
        return projectCountRepositoryCustom.incrementCount(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project count not found")));
    }

    @Override
    public Mono<ProjectCountResponseDTO> decrementCount(UUID projectId) {
        return projectCountRepository.findByProject(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project count not found")))
                .flatMap(pc -> {
                    int newCount = Math.max(0, pc.getTicketCount() - 1);
                    pc.setTicketCount(newCount);
                    return projectCountRepository.save(pc);
                })
                .map(updated -> ProjectCountResponseDTO.builder()
                        .id(updated.getId())
                        .project(updated.getProject())
                        .ticketCount(updated.getTicketCount())
                        .createdAt(updated.getCreatedAt())
                        .updatedAt(updated.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public Mono<ProjectCountResponseDTO> setCount(UUID projectId, Integer newCount) {
        return projectCountRepository.findByProject(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project count not found")))
                .flatMap(pc -> {
                    pc.setTicketCount(Math.max(0, newCount));
                    return projectCountRepository.save(pc);
                })
                .map(updated -> ProjectCountResponseDTO.builder()
                        .id(updated.getId())
                        .project(updated.getProject())
                        .ticketCount(updated.getTicketCount())
                        .createdAt(updated.getCreatedAt())
                        .updatedAt(updated.getUpdatedAt())
                        .build()
                );
    }

    @Override
    public Mono<Void> deleteProjectCount(UUID projectId) {
        return projectCountRepository.findByProject(projectId)
                .switchIfEmpty(Mono.error(new RuntimeException("Project count not found")))
                .flatMap(pc -> projectCountRepository.delete(pc));
    }

}
