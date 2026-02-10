package com.shxv.authenticationTemplate.Ticket.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Comment.DTO.CommentResponse;
import com.shxv.authenticationTemplate.Comment.Model.TicketComment;
import com.shxv.authenticationTemplate.Comment.Repository.TicketCommentRepository;
import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import com.shxv.authenticationTemplate.Project.Model.Project;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.Service.ProjectMemberService;
import com.shxv.authenticationTemplate.ProjectMember.Util.PermissionResolver;
import com.shxv.authenticationTemplate.Ticket.DTO.*;
import com.shxv.authenticationTemplate.Ticket.Enum.TicketPriority;
import com.shxv.authenticationTemplate.Ticket.Model.Ticket;
import com.shxv.authenticationTemplate.Ticket.Model.TicketStatus;
import com.shxv.authenticationTemplate.Ticket.Model.TicketType;
import com.shxv.authenticationTemplate.Ticket.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketTypeRepository ticketTypeRepository;

    @Autowired
    TicketStatusRepository ticketStatusRepository;

    @Autowired
    TicketTypeStatusMappingRepository ticketTypeStatusMappingRepository;

    @Autowired
    TicketStatusTransitionRepository ticketStatusTransitionRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TicketCommentRepository ticketCommentRepository;

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    UserService userService;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PermissionResolver permissionResolver;

    @Override
    public Mono<TicketResponse> createTicket(TicketCreate ticketCreate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> projectRepository.findById(ticketCreate.getProject())
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID")))
                        .flatMap(project -> {
                            Mono<Boolean> assigneeCheck =
                                    ticketCreate.getAssignedTo() == null
                                            ? Mono.just(true)
                                            : projectMemberService.isMember(project.getId(), ticketCreate.getAssignedTo());

                            return assigneeCheck.flatMap(isMember -> {
                                if (!isMember) {
                                    return Mono.error(new RuntimeException("Assignee is not a member of this project"));
                                }

                                if (!project.getStatus().equals(ProjectStatus.ACTIVE)) {
                                    return Mono.error(new RuntimeException("The project is not active"));
                                }

                                return permissionResolver.hasPermission("CREATE_TICKET", adminCheckResponse.getUserId(), project.getId())
                                        .flatMap(hasPermission -> {
                                            if (!hasPermission && !adminCheckResponse.getIsAdmin() && !project.getOwner().equals(adminCheckResponse.getUserId())) {
                                                return Mono.error(new RuntimeException("You are not allowed to create tickets in this project"));
                                            }

                                            return ticketTypeRepository.findById(ticketCreate.getType())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Invalid ticket type ID")))
                                                    .flatMap(ticketType -> ticketStatusRepository.findByKey("OPEN")
                                                            .switchIfEmpty(Mono.error(new RuntimeException("Initial status OPEN not configured")))
                                                            .flatMap(ticketStatus -> ticketRepository.findMaxTicketNumberByProject(project.getId())
                                                                    .defaultIfEmpty(0)
                                                                    .flatMap(maxNumber -> {
                                                                        Ticket ticket = Ticket.builder()
                                                                                .project(project.getId())
                                                                                .ticketNumber(maxNumber + 1)
                                                                                .type(ticketType.getId())
                                                                                .status(ticketStatus.getId())
                                                                                .priority(TicketPriority.from(ticketCreate.getPriority()))
                                                                                .title(ticketCreate.getTitle())
                                                                                .description(ticketCreate.getDescription())
                                                                                .createdBy(adminCheckResponse.getUserId())
                                                                                .deadline(ticketCreate.getDeadline())
                                                                                .assignedTo(ticketCreate.getAssignedTo() == null ? null : ticketCreate.getAssignedTo())
                                                                                .assignedBy(ticketCreate.getAssignedTo() == null ? null : adminCheckResponse.getUserId())
                                                                                .createdAt(LocalDateTime.now())
                                                                                .updatedAt(LocalDateTime.now())
                                                                                .build();

                                                                        if (ticketCreate.getParentTicket() == null) {
                                                                            return ticketRepository.save(ticket);
                                                                        }

                                                                        return ticketRepository.findById(ticketCreate.getParentTicket())
                                                                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid parent ticket ID")))
                                                                                .flatMap(parentTicket -> {
                                                                                    if (!parentTicket.getProject().equals(project.getId())) {
                                                                                        return Mono.error(new RuntimeException("Parent ticket does not belong to this project"));
                                                                                    }

                                                                                    ticket.setParentTicket(parentTicket.getId());

                                                                                    return ticketRepository.save(ticket);
                                                                                });
                                                                    })
                                                                    .flatMap(this::mapToResponse)
                                                            )
                                                    );
                                        });
                            });
                        })
                );
    }

    @Override
    public Flux<TicketResponse> getAllTickets() {
        return userRoleUtil.isAdmin()
                .flatMapMany(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return ticketRepository.findAll()
                                .flatMap(this::mapToResponse);
                    }
                    return ticketRepository.findByActive(Boolean.TRUE)
                            .flatMap(this::mapToResponse);
                });
    }

    @Override
    public Flux<TicketResponse> getAllTicketsByProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid project ID")))
                .flatMapMany(project -> userRoleUtil.isAdmin()
                        .flatMapMany(adminCheck -> {
                            if (!adminCheck.getIsAdmin()
                                    && project.getStatus() != ProjectStatus.ACTIVE) {
                                return Flux.error(new IllegalStateException("The project is not active"));
                            }

                            Flux<Ticket> ticketFlux = adminCheck.getIsAdmin()
                                    ? ticketRepository.findByProject(projectId)
                                    : ticketRepository.findByProjectAndActive(projectId, true);

                            return ticketFlux.flatMap(this::mapToResponse);
                        })
                );
    }

    @Override
    public Mono<TicketResponse> getTicketById(UUID ticketId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                                .flatMap(this::mapToResponse);
                    }
                    return ticketRepository.findByIdAndActive(ticketId, true)
                            .switchIfEmpty(Mono.error(new RuntimeException("No ticket active found with this ID")))
                            .flatMap(this::mapToResponse);
                });
    }

    @Override
    public Mono<TicketResponse> updateTicket(UUID ticketId, TicketUpdate ticketUpdate) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(ticketId)
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                        .flatMap(ticket -> {
                                    if (!ticket.getActive()) {
                                        return Mono.error(new RuntimeException("This ticket is not currently active"));
                                    }

                                    return projectRepository.findById(ticket.getProject())
                                            .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in ticket")))
                                            .flatMap(project -> {

                                                UUID userId = adminCheckResponse.getUserId();

                                                if (!adminCheckResponse.getIsAdmin()
                                                        && !userId.equals(ticket.getCreatedBy())
                                                        && !project.getOwner().equals(adminCheckResponse.getUserId())) {
                                                    return Mono.error(new RuntimeException("You are not allowed to edit this ticket"));
                                                }


                                                if (ticketUpdate.getTitle() != null && !ticketUpdate.getTitle().trim().isEmpty()) {
                                                    ticket.setTitle(ticketUpdate.getTitle());
                                                }

                                                if (ticketUpdate.getDescription() != null && !ticketUpdate.getDescription().trim().isEmpty()) {
                                                    ticket.setDescription(ticketUpdate.getDescription());
                                                }

                                                if (ticketUpdate.getDeadline() != null) {
                                                    ticket.setDeadline(ticketUpdate.getDeadline());
                                                }


                                                return ticketRepository.save(ticket)
                                                        .flatMap(this::mapToResponse);
                                            });
                                }

                        )
                );
    }

    @Override
    public Mono<TicketResponse> deleteTicket(UUID ticketId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(ticketId)
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                        .flatMap(ticket -> projectRepository.findById(ticket.getProject())
                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in the ticket")))
                                .flatMap(project -> {
                                    if (!ticket.getActive()) {
                                        return Mono.error(new RuntimeException("This ticket is deactivated"));
                                    }

                                    if (!adminCheckResponse.getIsAdmin()
                                            && !ticket.getCreatedBy().equals(adminCheckResponse.getUserId())
                                            && !project.getOwner().equals(adminCheckResponse.getUserId())) {
                                        return Mono.error(new RuntimeException("You are not allowed to delete this ticket"));
                                    }

                                    return ticketRepository.deleteById(ticket.getId())
                                            .thenReturn(ticket)
                                            .flatMap(this::mapToResponse);
                                })
                        )
                );
    }

    @Override
    public Mono<TicketResponse> assignTicket(AssigneeUpdate assigneeUpdate) {
        if (assigneeUpdate.getAssignee() == null) {
            return Mono.error(new RuntimeException("Assignee cannot be null"));
        }

        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(assigneeUpdate.getTicketId())
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with thid ID")))
                        .flatMap(ticket -> projectRepository.findById(ticket.getProject())
                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in ticket")))
                                .flatMap(project -> {
                                    Mono<Boolean> assigneeCheck =
                                            assigneeUpdate.getAssignee() == null
                                                    ? Mono.just(true)
                                                    : projectMemberService.isMember(project.getId(), assigneeUpdate.getAssignee());

                                    return assigneeCheck.flatMap(isMember -> {
                                        if (!isMember) {
                                            return Mono.error(new RuntimeException("Assignee is not a member of this project"));
                                        }

                                        if (!ticket.getActive()) {
                                            return Mono.error(new RuntimeException("Cannot assign an inactive ticket"));
                                        }

                                        if (!adminCheckResponse.getIsAdmin() &&
                                                !ticket.getCreatedBy().equals(adminCheckResponse.getUserId()) &&
                                                !project.getOwner().equals(adminCheckResponse.getUserId())) {
                                            return Mono.error(new RuntimeException("You are not allowed to assign the ticket"));
                                        }

                                        ticket.setAssignedTo(assigneeUpdate.getAssignee());
                                        ticket.setAssignedBy(adminCheckResponse.getUserId());

                                        return ticketRepository.save(ticket)
                                                .flatMap(this::mapToResponse);
                                    });
                                })
                        )
                );
    }

    @Override
    public Mono<TicketResponse> updatePriority(PriorityUpdate priorityUpdate) {
        TicketPriority newPriority;
        try {
            newPriority = TicketPriority.from(priorityUpdate.getPriority());
        } catch (IllegalArgumentException ex) {
            return Mono.error(new RuntimeException("Invalid ticket priority"));
        }

        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(priorityUpdate.getTicketId())
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                        .flatMap(ticket -> projectRepository.findById(ticket.getProject())
                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in ticket")))
                                .flatMap(project -> {
                                    if (!ticket.getActive()) {
                                        return Mono.error(new RuntimeException("Cannot update an inactive ticket"));
                                    }

                                    if (ticket.getPriority().equals(newPriority)) {
                                        return Mono.error(new RuntimeException("Priority is already " + newPriority));
                                    }

                                    if (!adminCheckResponse.getIsAdmin() &&
                                            !ticket.getCreatedBy().equals(adminCheckResponse.getUserId()) &&
                                            !project.getOwner().equals(adminCheckResponse.getUserId())) {
                                        return Mono.error(new RuntimeException("You are not allowed to assign the ticket"));
                                    }

                                    ticket.setPriority(newPriority);

                                    return ticketRepository.save(ticket)
                                            .flatMap(this::mapToResponse);
                                })
                        )
                );
    }

    @Override
    public Mono<TicketResponse> updateStatus(StatusUpdate statusUpdate) {
        if (statusUpdate.getStatus() == null || statusUpdate.getStatus().trim().isEmpty()) {
            return Mono.error(new RuntimeException("Status cannot be null or empty"));
        }

        String newStatus = statusUpdate.getStatus().trim().toUpperCase();

        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(statusUpdate.getTicketId())
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                        .flatMap(ticket -> {
                            if (!ticket.getActive()) {
                                return Mono.error(new RuntimeException("Cannot update an inactive ticket"));
                            }

                            return projectRepository.findById(ticket.getProject())
                                    .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in ticket")))
                                    .flatMap(project -> ticketStatusRepository.findByKey(newStatus)
                                            .switchIfEmpty(Mono.error(new RuntimeException("Invalid status")))
                                            .flatMap(ticketStatus -> ticketTypeStatusMappingRepository.findByTicketTypeIdAndTicketStatusId(ticket.getType(), ticketStatus.getId())
                                                    .switchIfEmpty(Mono.error(new RuntimeException("Invalid status for this type of ticket")))
                                                    .flatMap(ticketTypeStatusMapping -> ticketStatusTransitionRepository.findByFromStatusIdAndToStatusIdAndTicketTypeId(ticket.getStatus(), ticketStatus.getId(), ticket.getType())
                                                            .switchIfEmpty(Mono.error(new RuntimeException("Invalid transition for this status")))
                                                            .flatMap(ticketStatusTransition -> {
                                                                UUID userId = adminCheckResponse.getUserId();

                                                                boolean isAdmin = adminCheckResponse.getIsAdmin();
                                                                boolean isProjectOwner = project.getOwner().equals(userId);
                                                                boolean isCreator = ticket.getCreatedBy().equals(userId);
                                                                boolean isAssignee = ticket.getAssignedTo() != null
                                                                        && ticket.getAssignedTo().equals(userId);

                                                                String targetStatus = ticketStatus.getKey(); // use DB value, not user input

                                                                if (!isAdmin) {

                                                                    boolean allowed = isProjectOwner && PROJECT_OWNER_ALLOWED.contains(targetStatus);

                                                                    if (isCreator && CREATOR_ALLOWED.contains(targetStatus)) {
                                                                        allowed = true;
                                                                    }

                                                                    if (isAssignee && ASSIGNEE_ALLOWED.contains(targetStatus)) {
                                                                        allowed = true;
                                                                    }

                                                                    if (!allowed) {
                                                                        return Mono.error(new RuntimeException(
                                                                                "You are not allowed to change ticket status to " + targetStatus
                                                                        ));
                                                                    }
                                                                }

                                                                ticket.setStatus(ticketStatus.getId());

                                                                return ticketRepository.save(ticket)
                                                                        .flatMap(this::mapToResponse);
                                                            })
                                                    )
                                            )
                                    );
                        })
                );
    }

    @Override
    public Mono<TicketDetailsResponse> getTicketDetailsById(UUID ticketId) {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> {
                    if (adminCheckResponse.getIsAdmin()) {
                        return ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")));
                    }
                    return ticketRepository.findByIdAndActive(ticketId, true)
                            .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")));
                })
                .flatMap(ticket -> Mono.zip(
                                        projectRepository.findById(ticket.getProject()),
                                        userService.getUserById(ticket.getCreatedBy()),
                                        Mono.justOrEmpty(ticket.getAssignedBy())
                                                .flatMap(userService::getUserById)
                                                .onErrorReturn(UserResponse.builder().build())
                                                .defaultIfEmpty(UserResponse.builder().build()),
                                        Mono.justOrEmpty(ticket.getAssignedTo())
                                                .flatMap(userService::getUserById)
                                                .onErrorReturn(UserResponse.builder().build())
                                                .defaultIfEmpty(UserResponse.builder().build()),
                                        ticketStatusRepository.findById(ticket.getStatus()),
                                        ticketTypeRepository.findById(ticket.getType()),
                                        ticketCommentRepository.findAllByTicketId(ticket.getId()).flatMap(ticketComment -> Mono.fromCallable(() -> objectMapper.readTree(ticketComment.getContent()))
                                                .flatMap(content -> userService.getUserById(ticketComment.getCreatedBy())
                                                        .map(userResponse -> CommentResponse.builder()
                                                                .id(ticketComment.getId())
                                                                .ticketId(ticketComment.getTicketId())
                                                                .content(content)
                                                                .createdBy(userResponse)
                                                                .createdAt(ticketComment.getCreatedAt())
                                                                .updatedAt(ticketComment.getUpdatedAt())
                                                                .build())
                                                )).collectList(),
                                        ticketRepository.findAllByParentTicket(ticket.getId()).flatMap(this::mapToResponse).collectList()
                                )
                                .map(tuple -> {
                                    Project project = tuple.getT1();
                                    UserResponse createdBy = tuple.getT2();
                                    UserResponse assignedBy = tuple.getT3();
                                    UserResponse assignedTo = tuple.getT4();
                                    TicketStatus ticketStatus = tuple.getT5();
                                    TicketType ticketType = tuple.getT6();
                                    List<CommentResponse> comments = tuple.getT7();
                                    List<TicketResponse> subTickets = tuple.getT8();

                                    return TicketDetailsResponse.builder()
                                            .id(ticket.getId())
                                            .project(project)
                                            .ticketCode(project.getProjectCode() + "-" + ticket.getTicketNumber())
                                            .title(ticket.getTitle())
                                            .description(ticket.getDescription())
                                            .status(ticketStatus.getKey())
                                            .priority(ticket.getPriority().toString())
                                            .type(ticketType.getKey())
                                            .isBase(ticket.getParentTicket() == null)
                                            .comments(comments)
                                            .subTickets(subTickets)
                                            .parentTicket(ticket.getParentTicket())
                                            .active(ticket.getActive())
                                            .deadline(ticket.getDeadline())
                                            .createdBy(createdBy)
                                            .assignedTo(assignedTo.getId() == null ? null : assignedTo)
                                            .assignedBy(assignedBy.getId() == null ? null : assignedBy)
                                            .createdAt(ticket.getCreatedAt())
                                            .updatedAt(ticket.getUpdatedAt())
                                            .build();
                                })
                );
    }

    @Override
    public Flux<TicketType> getTicketTypes() {
        return ticketTypeRepository.findAll();
    }

    @Override
    public Mono<TransitionResponse> getValidTransitions(String status, String type) {
        return ticketStatusRepository.findByKey(status)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid status")))
                .flatMap(ticketStatus -> ticketTypeRepository.findByKey(type)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid type")))
                        .flatMap(ticketType -> ticketTypeStatusMappingRepository.findByTicketTypeIdAndTicketStatusId(ticketType.getId(), ticketStatus.getId())
                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid status for this type")))
                                .flatMap(ticketTypeStatusMapping -> ticketStatusTransitionRepository.findAllByFromStatusIdAndTicketTypeId(ticketStatus.getId(), ticketType.getId())
                                        .flatMap(ticketStatusTransition -> ticketStatusRepository.findById(ticketStatusTransition.getToStatusId())
                                                .map(TicketStatus::getKey))
                                        .collectList()
                                ).map(list -> TransitionResponse.builder()
                                        .status(ticketStatus.getKey())
                                        .nextStatusOptions(list)
                                        .build())
                        )
                );
    }

    Set<String> ASSIGNEE_ALLOWED = Set.of("ACKNOWLEDGED", "INVESTIGATING", "ANALYSIS", "IN PROGRESS", "REVIEW", "MITIGATED", "RESOLVED", "ON HOLD", "BLOCKED");
    Set<String> CREATOR_ALLOWED = Set.of("OPEN", "REVIEW", "APPROVED", "REJECTED", "CANCELLED", "DUPLICATE", "WONT FIX", "CLOSED");
    Set<String> PROJECT_OWNER_ALLOWED = Set.of("OPEN", "ON HOLD", "BLOCKED", "REVIEW", "APPROVAL PENDING", "APPROVED", "REJECTED", "WONT FIX", "CANCELLED", "DUPLICATE", "RESOLVED", "CLOSED");

    //HELPER METHODS
    private Mono<TicketResponse> mapToResponse(Ticket ticket) {
        return Mono.zip(
                projectRepository.findById(ticket.getProject()),
                userService.getUserById(ticket.getCreatedBy()),
                Mono.justOrEmpty(ticket.getAssignedBy())
                        .flatMap(userService::getUserById)
                        .onErrorReturn(UserResponse.builder().build())
                        .defaultIfEmpty(UserResponse.builder().build()),
                Mono.justOrEmpty(ticket.getAssignedTo())
                        .flatMap(userService::getUserById)
                        .onErrorReturn(UserResponse.builder().build())
                        .defaultIfEmpty(UserResponse.builder().build()),
                ticketStatusRepository.findById(ticket.getStatus()),
                ticketTypeRepository.findById(ticket.getType())
        ).map(tuple -> {
            Project project = tuple.getT1();
            UserResponse createdBy = tuple.getT2();
            UserResponse assignedBy = tuple.getT3();
            UserResponse assignedTo = tuple.getT4();
            TicketStatus ticketStatus = tuple.getT5();
            TicketType ticketType = tuple.getT6();

            return TicketResponse.builder()
                    .id(ticket.getId())
                    .project(project)
                    .ticketCode(project.getProjectCode() + "-" + ticket.getTicketNumber())
                    .title(ticket.getTitle())
                    .description(ticket.getDescription())
                    .status(ticketStatus.getKey())
                    .priority(ticket.getPriority().toString())
                    .type(ticketType.getKey())
                    .isBase(ticket.getParentTicket() == null)
                    .parentTicket(ticket.getParentTicket())
                    .active(ticket.getActive())
                    .deadline(ticket.getDeadline())
                    .createdBy(createdBy)
                    .assignedTo(assignedTo.getId() == null ? null : assignedTo)
                    .assignedBy(assignedBy.getId() == null ? null : assignedBy)
                    .createdAt(ticket.getCreatedAt())
                    .updatedAt(ticket.getUpdatedAt())
                    .build();
        });
    }

}
