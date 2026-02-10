package com.shxv.authenticationTemplate.Project.Service;

import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Project.DTO.ProjectCreate;
import com.shxv.authenticationTemplate.Project.DTO.ProjectDetailResponse;
import com.shxv.authenticationTemplate.Project.DTO.ProjectResponse;
import com.shxv.authenticationTemplate.Project.DTO.ProjectUpdate;
import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import com.shxv.authenticationTemplate.Project.Model.Project;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.Ticket.Repository.TicketRepository;
import com.shxv.authenticationTemplate.Ticket.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    UserService userService;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketService ticketService;

    @Override
    public Mono<ProjectResponse> createProject(ProjectCreate projectCreate) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> projectRepository.save(
                                Project.builder()
                                        .name(projectCreate.getName())
                                        .projectCode(projectCreate.getProjectCode())
                                        .description(projectCreate.getDescription())
                                        .createdBy(currentUserId)
                                        .owner(projectCreate.getOwner())
                                        .status(ProjectStatus.ACTIVE)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build())
                        .flatMap(this::mapToResponse));
    }

    @Override
    public Flux<ProjectResponse> getAllProjects() {
        return userRoleUtil.isAdmin()
                .flatMapMany(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return projectRepository.findAll()
                                .flatMap(this::mapToResponse);
                    }

                    return projectRepository.findAllByStatusNot(ProjectStatus.SUSPENDED)
                            .flatMap(this::mapToResponse);
                });
    }

    @Override
    public Mono<ProjectResponse> getProjectById(UUID id) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(id)
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (!adminCheckResponse.getIsAdmin() && project.getStatus().equals(ProjectStatus.SUSPENDED)) {
                                return Mono.error(new RuntimeException("No project with this id exists"));
                            }

                            return Mono.just(project);
                        })
                        .flatMap(this::mapToResponse));
    }

    @Override
    public Mono<ProjectDetailResponse> getProjectDetail(UUID id) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(id)
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (!adminCheckResponse.getIsAdmin() && project.getStatus().equals(ProjectStatus.SUSPENDED)) {
                                return Mono.error(new RuntimeException("No project with this id exists"));
                            }

                            return Mono.just(project);
                        })
                        .flatMap(this::mapToDetail));
    }

    @Override
    public Mono<ProjectResponse> getProjectByCode(String projectCode) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findByProjectCode(projectCode)
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (!adminCheckResponse.getIsAdmin() && project.getStatus().equals(ProjectStatus.SUSPENDED)) {
                                return Mono.error(new RuntimeException("No project with this id exists"));
                            }

                            return Mono.just(project);
                        })
                        .flatMap(this::mapToResponse));
    }

    @Override
    public Flux<ProjectResponse> getProjectsByOwner(UUID owner) {
        return userRoleUtil.isAdmin()
                .flatMapMany(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return projectRepository.findAllByOwner(owner)
                                .flatMap(this::mapToResponse);
                    }

                    return projectRepository.findAllByOwnerAndStatusNot(owner, ProjectStatus.SUSPENDED)
                            .flatMap(this::mapToResponse);
                });
    }

    @Override
    public Mono<ProjectResponse> updateProject(UUID id, ProjectUpdate projectUpdate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(id)
                        .switchIfEmpty(Mono.error(
                                new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (
                                    !adminCheckResponse.getIsAdmin() &&
                                            !project.getCreatedBy().equals(adminCheckResponse.getUserId())
                            ) {
                                return Mono.error(new RuntimeException("You are not allowed to do changes to this project"));
                            }

                            if (!project.getStatus().equals(ProjectStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This project is not currently active"));
                            }

                            if (projectUpdate.getName() != null
                                    && !projectUpdate.getName().isBlank()) {
                                project.setName(projectUpdate.getName());
                            }

                            if (projectUpdate.getProjectCode() != null
                                    && !projectUpdate.getProjectCode().isBlank()) {
                                project.setProjectCode(projectUpdate.getProjectCode());
                            }

                            if (projectUpdate.getDescription() != null
                                    && !projectUpdate.getDescription().isBlank()) {
                                project.setDescription(projectUpdate.getDescription());
                            }

                            project.setUpdatedAt(LocalDateTime.now());
                            return projectRepository.save(project)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    @Override
    public Mono<ProjectResponse> deleteProject(UUID id) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(id)
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (
                                    !adminCheckResponse.getIsAdmin() &&
                                            !project.getCreatedBy().equals(adminCheckResponse.getUserId())
                            ) {
                                return Mono.error(new RuntimeException("You are not allowed to delete this project"));
                            }

                            return projectRepository.deleteById(id)
                                    .thenReturn(project)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    @Override
    public Mono<ProjectResponse> archiveProject(UUID projectId) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("No project with this id exists")))
                        .flatMap(project -> {
                            if (project.getStatus() != ProjectStatus.ACTIVE) {
                                return Mono.error(new IllegalStateException("This project is not currently active"));
                            }

                            if (!currentUserId.equals(project.getCreatedBy()) &&
                                    !currentUserId.equals(project.getOwner())) {
                                return Mono.error(new SecurityException("You are not allowed to modify this project"));
                            }

                            Mono<Void> deactivateTickets =
                                    ticketRepository.findByProject(projectId)
                                            .flatMap(ticket -> {
                                                ticket.setActive(false);
                                                ticket.setUpdatedAt(LocalDateTime.now());
                                                return ticketRepository.save(ticket);
                                            })
                                            .then();

                            return deactivateTickets.then(
                                    Mono.defer(() -> {
                                        project.setStatus(ProjectStatus.ARCHIVED);
                                        project.setUpdatedAt(LocalDateTime.now());
                                        return projectRepository.save(project);
                                    })
                            );
                        })
                        .flatMap(this::mapToResponse)
                );
    }

    @Override
    public Mono<ProjectResponse> suspendProject(UUID projectId) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("No project with this id exists")))
                        .flatMap(project -> {

                            if (project.getStatus() != ProjectStatus.ACTIVE) {
                                return Mono.error(new IllegalStateException("This project is not currently active"));
                            }

                            if (!currentUserId.equals(project.getCreatedBy()) &&
                                    !currentUserId.equals(project.getOwner())) {
                                return Mono.error(new SecurityException("You are not allowed to modify this project"));
                            }

                            Mono<Void> deactivateTickets = ticketRepository.findByProject(projectId)
                                    .flatMap(ticket -> {
                                        ticket.setActive(false);
                                        ticket.setUpdatedAt(LocalDateTime.now());
                                        return ticketRepository.save(ticket);
                                    })
                                    .then();

                            return deactivateTickets.then(
                                    Mono.defer(() -> {
                                        project.setStatus(ProjectStatus.SUSPENDED);
                                        project.setUpdatedAt(LocalDateTime.now());
                                        return projectRepository.save(project);
                                    })
                            );
                        })
                        .flatMap(this::mapToResponse)
                );
    }

    @Override
    public Mono<ProjectResponse> restoreProject(UUID projectId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheck -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("No project with this id exists")))
                        .flatMap(project -> {
                            if (project.getStatus() == ProjectStatus.ACTIVE) {
                                return Mono.error(new IllegalStateException("Project is already active"));
                            }

                            if (!adminCheck.getIsAdmin() && project.getStatus() == ProjectStatus.SUSPENDED) {
                                return Mono.error(new SecurityException("You are not allowed to restore this project"));
                            }

                            if (project.getStatus() == ProjectStatus.ARCHIVED
                                    && !adminCheck.getUserId().equals(project.getCreatedBy())
                                    && !adminCheck.getIsAdmin()) {
                                return Mono.error(new SecurityException("You are not allowed to restore this project"));
                            }

                            Mono<Void> activateTickets = ticketRepository.findByProject(projectId)
                                    .flatMap(ticket -> {
                                        ticket.setActive(true);
                                        ticket.setUpdatedAt(LocalDateTime.now());
                                        return ticketRepository.save(ticket);
                                    })
                                    .then();

                            return activateTickets.then(
                                    Mono.defer(() -> {
                                        project.setStatus(ProjectStatus.ACTIVE);
                                        project.setUpdatedAt(LocalDateTime.now());
                                        return projectRepository.save(project);
                                    })
                            );
                        })
                        .flatMap(this::mapToResponse)
                );
    }

    @Override
    public Mono<ProjectResponse> transferOwnership(UUID projectId, UUID newOwnerId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(projectId)
                        .switchIfEmpty(Mono.error(new RuntimeException("No project with this id exists")))
                        .flatMap(project -> {
                            if (
                                    !adminCheckResponse.getIsAdmin() &&
                                            !project.getCreatedBy().equals(adminCheckResponse.getUserId())
                            ) {
                                return Mono.error(new RuntimeException("You are not allowed to do changes to this project"));
                            }

                            if (!project.getStatus().equals(ProjectStatus.ACTIVE)) {
                                return Mono.error(new RuntimeException("This project is not currently active"));
                            }

                            if (newOwnerId != null && newOwnerId.equals(project.getOwner())) {
                                return mapToResponse(project);
                            }

                            if (newOwnerId == null) {
                                return Mono.error(new RuntimeException("New owner id is required"));
                            }

                            project.setOwner(newOwnerId);
                            project.setUpdatedAt(LocalDateTime.now());

                            return projectRepository.save(project)
                                    .flatMap(this::mapToResponse);
                        }));
    }

    //HELPER METHODS
    private Mono<ProjectResponse> mapToResponse(Project project) {
        return Mono.zip(
                userService.getUserById(project.getCreatedBy()),
                userService.getUserById(project.getOwner())
        ).map(tuple ->
                ProjectResponse.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .projectCode(project.getProjectCode())
                        .description(project.getDescription())
                        .createdBy(tuple.getT1())
                        .owner(tuple.getT2())
                        .status(project.getStatus().getLabel())
                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getUpdatedAt())
                        .build()
        );
    }

    private Mono<ProjectDetailResponse> mapToDetail(Project project) {
        return Mono.zip(
                userService.getUserById(project.getCreatedBy()),
                userService.getUserById(project.getOwner()),
                ticketService.getAllTicketsByProject(project.getId()).collectList()
        ).map(tuple ->
                ProjectDetailResponse.builder()
                        .id(project.getId())
                        .name(project.getName())
                        .projectCode(project.getProjectCode())
                        .description(project.getDescription())
                        .createdBy(tuple.getT1())
                        .owner(tuple.getT2())
                        .tickets(tuple.getT3())
                        .status(project.getStatus().getLabel())
                        .createdAt(project.getCreatedAt())
                        .updatedAt(project.getUpdatedAt())
                        .build()
        );
    }
}
