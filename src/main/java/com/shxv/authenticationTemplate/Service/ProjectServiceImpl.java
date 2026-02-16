package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.DTO.ProjectCreateRequestDTO;
import com.shxv.authenticationTemplate.DTO.ProjectResponseDTO;
import com.shxv.authenticationTemplate.DTO.TicketResponseDTO;
import com.shxv.authenticationTemplate.Model.Project;
import com.shxv.authenticationTemplate.Model.ProjectMember;
import com.shxv.authenticationTemplate.Model.Ticket;
import com.shxv.authenticationTemplate.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.Repository.TicketRepository;
import com.shxv.authenticationTemplate.Role.Service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    ProjectCountService projectCountService;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    PermissionService permissionService;

    @Autowired
    UserService userService;

    @Override
    public Flux<ProjectResponseDTO> getAllProjects() {

        return userRoleUtil.getUserId()
                .flatMapMany(userId ->
                        projectMemberService.getProjectsByUser(userId)
                                .map(ProjectMember::getProject)
                                .collectList()
                                .flatMapMany(projectIds ->
                                        projectRepository.findAllById(projectIds)
                                )
                                .flatMap(createdProject -> createProjectResponseDTO(createdProject))
                );
    }

    @Override
    public Mono<ProjectResponseDTO> getProjectById(UUID uuid) {
        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        projectMemberService.isMember(uuid, userId)
                                .flatMap(isMember -> {
                                    if (isMember) {
                                        return projectRepository.findById(uuid)
                                                .switchIfEmpty(Mono.error(new RuntimeException("Project not found")));
                                    } else {
                                        return Mono.error(new RuntimeException("You are not authorized to view this project"));
                                    }
                                })
                )
                .flatMap(createdProject -> createProjectResponseDTO(createdProject));
    }

    @Override
    public Mono<ProjectResponseDTO> createProject(ProjectCreateRequestDTO dto) {

        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> {

                    Mono<Boolean> permissionCheckMono =
                            dto.getOwner() == null
                                    ? Mono.just(true)
                                    : permissionService.hasUserPermission(
                                    dto.getOwner(),
                                    "CREATE_TICKET"
                            );

                    return permissionCheckMono.flatMap(hasPermission -> {
                        if (!hasPermission) {
                            return Mono.error(new RuntimeException("Invalid owner"));
                        }

                        return projectRepository.save(
                                        Project.builder()
                                                .name(dto.getName())
                                                .projectCode(dto.getProjectCode())
                                                .description(dto.getDescription())
                                                .createdBy(currentUserId)
                                                .owner(dto.getOwner())
                                                .createdAt(LocalDateTime.now())
                                                .updatedAt(LocalDateTime.now())
                                                .build()
                                )
                                .flatMap(savedProject ->
                                        projectCountService.createProjectCount(savedProject.getId())
                                                .thenReturn(savedProject)
                                )
                                .flatMap(savedProject ->
                                        projectMemberService.addMember(savedProject.getId(), currentUserId).thenReturn(savedProject)
                                )
                                .flatMap(savedProject -> {
                                    if (dto.getOwner() != null && !dto.getOwner().equals(currentUserId)) {
                                        return projectMemberService.addMember(savedProject.getId(), dto.getOwner())
                                                .thenReturn(savedProject);
                                    }
                                    return Mono.just(savedProject);
                                })
                                .flatMap(project -> createProjectResponseDTO(project));
                    });
                })
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<ProjectResponseDTO> updateProject(UUID uuid, ProjectCreateRequestDTO dto) {

        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        projectMemberService.isMember(uuid, userId)
                                .flatMap(isMember -> {
                                    if (!isMember) {
                                        return Mono.error(new RuntimeException("You are not authorized to edit this project"));
                                    }

                                    return projectRepository.findById(uuid)
                                            .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                                            .flatMap(project -> {
                                                if (!project.getIsActive()) {
                                                    return Mono.error(new RuntimeException("Project queued for deletion"));
                                                }
                                                return Mono.just(project);
                                            })
                                            .flatMap(existingProject -> {

                                                Mono<Boolean> ownerPermissionCheck =
                                                        dto.getOwner() == null
                                                                ? Mono.just(true)
                                                                : permissionService.hasUserPermission(dto.getOwner(), "CREATE_TICKET");

                                                return ownerPermissionCheck.flatMap(hasPermission -> {
                                                    if (!hasPermission) {
                                                        return Mono.error(new RuntimeException("Invalid owner"));
                                                    }

                                                    if (dto.getName() != null) {
                                                        existingProject.setName(dto.getName());
                                                    }

                                                    if (dto.getDescription() != null) {
                                                        existingProject.setDescription(dto.getDescription());
                                                    }

                                                    if (dto.getProjectCode() != null) {
                                                        existingProject.setProjectCode(dto.getProjectCode());
                                                    }

                                                    if (dto.getOwner() != null) {
                                                        existingProject.setOwner(dto.getOwner());
                                                    }

                                                    existingProject.setUpdatedAt(LocalDateTime.now());

                                                    return projectRepository.save(existingProject);
                                                });
                                            })
                                            .flatMap(savedProject -> {
                                                if (dto.getOwner() == null) {
                                                    return Mono.just(savedProject);
                                                }

                                                return projectMemberService
                                                        .isMember(savedProject.getId(), dto.getOwner())
                                                        .flatMap(isOwnerMember -> {
                                                            if (isOwnerMember) {
                                                                return Mono.just(savedProject);
                                                            }
                                                            return projectMemberService
                                                                    .addMember(savedProject.getId(), dto.getOwner())
                                                                    .thenReturn(savedProject);
                                                        });
                                            });
                                })
                )
                .flatMap(project -> createProjectResponseDTO(project))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<ProjectResponseDTO> deleteProject(UUID uuid) {

        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        projectMemberService.isMember(uuid, userId)
                                .flatMap(isMember -> {
                                    if (!isMember) {
                                        return Mono.error(new RuntimeException("You are not authorized to delete this project"));
                                    }

                                    return projectRepository.findById(uuid)
                                            .switchIfEmpty(Mono.error(new RuntimeException("Project not found")))
                                            .flatMap(project -> {
                                                if (!project.getIsActive()) {
                                                    return Mono.error(new RuntimeException("Project Already Queued for deletion"));
                                                } else {
                                                    return Mono.just(project);
                                                }
                                            })
                                            .flatMap(project -> {
                                                project.setIsActive(false);

                                                return projectRepository.save(project);
                                            })
                                            .flatMap(project -> createProjectResponseDTO(project));
                                })
                );
    }

    private Mono<ProjectResponseDTO> createProjectResponseDTO(Project project) {
        return Mono.zip(
                userService.getUserById(project.getCreatedBy()),
                Mono.justOrEmpty(project.getOwner())
                        .flatMap(userService::getUserById)
                        .onErrorReturn(UserResponse.builder().build())
                        .defaultIfEmpty(UserResponse.builder().build()),
                ticketRepository.findByProjectId(project.getId()).collectList()
        ).map(tuple -> {
            UserResponse createdBy = tuple.getT1();
            UserResponse owner = tuple.getT2();
            List<Ticket> tickets = tuple.getT3();

            return ProjectResponseDTO.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .projectCode(project.getProjectCode())
                    .description(project.getDescription())
                    .createdBy(createdBy)
                    .owner(owner.getId() == null ? null : owner)
                    .tickets(tickets)
                    .isActive(project.getIsActive())
                    .createdAt(project.getCreatedAt())
                    .updatedAt(project.getUpdatedAt())
                    .build();
        });
    }

}
