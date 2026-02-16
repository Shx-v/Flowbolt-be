package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.DTO.*;
import com.shxv.authenticationTemplate.Enum.TicketPriorityEnum;
import com.shxv.authenticationTemplate.Enum.TicketStatusEnum;
import com.shxv.authenticationTemplate.Enum.TicketTypeEnum;
import com.shxv.authenticationTemplate.Enum.TicketWorkflow;
import com.shxv.authenticationTemplate.Model.Ticket;
import com.shxv.authenticationTemplate.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectCountService projectCountService;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    UserService userService;

    @Autowired
    TransactionalOperator transactionalOperator;

    @Override
    public Mono<TicketResponseDTO> createTicket(TicketRequestDTO requestDTO) {

        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        projectMemberService.isMember(requestDTO.getProjectId(), userId)
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("You are not authorized to create tickets for this project")
                                ))
                                .then(ensureAssigneeIsProjectMember(requestDTO.getProjectId(),requestDTO.getAssignedTo()))
                                .then(
                                        projectService.getProjectById(requestDTO.getProjectId())
                                                .flatMap(project ->
                                                        projectCountService.incrementCount(requestDTO.getProjectId())
                                                                .flatMap(ticketNumber ->
                                                                        ticketRepository.save(
                                                                                Ticket.builder()
                                                                                        .projectId(requestDTO.getProjectId())
                                                                                        .ticketNumber(ticketNumber)
                                                                                        .title(requestDTO.getTitle())
                                                                                        .description(requestDTO.getDescription())
                                                                                        .status(
                                                                                                requestDTO.getAssignedTo() != null
                                                                                                        ? TicketStatusEnum.ASSIGNED
                                                                                                        : TicketStatusEnum.CREATED
                                                                                        )
                                                                                        .priority(TicketPriorityEnum.fromString(requestDTO.getPriority()))
                                                                                        .type(TicketTypeEnum.fromString(requestDTO.getType()))
                                                                                        .parentTicket(requestDTO.getParentTicket())
                                                                                        .createdBy(userId)
                                                                                        .assignedTo(requestDTO.getAssignedTo())
                                                                                        .assignedBy(
                                                                                                requestDTO.getAssignedTo() != null ? userId : null
                                                                                        )
                                                                                        .createdAt(LocalDateTime.now())
                                                                                        .updatedAt(LocalDateTime.now())
                                                                                        .build()
                                                                        )
                                                                )
                                                )
                                )
                )
                .flatMap(this::createTicketResponseDTO)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<TicketResponseDTO> getTicketById(UUID ticketId) {

        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("Ticket not found")
                                ))
                                .flatMap(ticket ->
                                        projectMemberService.isMember(ticket.getProjectId(), userId)
                                                .filter(Boolean::booleanValue)
                                                .switchIfEmpty(Mono.error(
                                                        new RuntimeException(
                                                                "You are not authorized to view tickets of this project"
                                                        )
                                                ))
                                                .then(createTicketResponseDTO(ticket))
                                )
                );
    }

    @Override
    public Flux<TicketResponseDTO> getAllTickets() {
        return userRoleUtil.getUserId()
                .flatMapMany(userId ->
                        ticketRepository.findAllByAssignedToOrAssignedByOrCreatedBy(userId)
                                .flatMap(ticket -> createTicketResponseDTO(ticket))
                );
    }

    @Override
    public Mono<TicketResponseDTO> updateTicket(UUID ticketId, TicketUpdateRequestDTO requestDTO) {

        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException("Ticket not found")
                                ))
                                .flatMap(existingTicket ->
                                        projectService.getProjectById(existingTicket.getProjectId())
                                                .filter(project -> {
                                                    boolean isAssigner =
                                                            existingTicket.getAssignedBy() != null &&
                                                                    existingTicket.getAssignedBy().equals(userId);

                                                    boolean isCreator =
                                                            existingTicket.getCreatedBy() != null &&
                                                                    existingTicket.getCreatedBy().equals(userId);

                                                    boolean isOwner =
                                                            project.getOwner() != null &&
                                                                    project.getOwner().getId().equals(userId);

                                                    return isAssigner || isOwner || isCreator;
                                                })
                                                .switchIfEmpty(Mono.error(
                                                        new RuntimeException(
                                                                "Only the assigner or the project owner can update this ticket"
                                                        )
                                                ))
                                                .then(
                                                        Mono.fromCallable(() -> {
                                                            if (requestDTO.getTitle() != null)
                                                                existingTicket.setTitle(requestDTO.getTitle());

                                                            if (requestDTO.getDescription() != null)
                                                                existingTicket.setDescription(requestDTO.getDescription());

                                                            if (requestDTO.getPriority() != null)
                                                                existingTicket.setPriority(
                                                                        TicketPriorityEnum.fromString(requestDTO.getPriority())
                                                                );

                                                            if (requestDTO.getType() != null)
                                                                existingTicket.setType(
                                                                        TicketTypeEnum.fromString(requestDTO.getType())
                                                                );

                                                            existingTicket.setUpdatedAt(LocalDateTime.now());
                                                            return existingTicket;
                                                        }).flatMap(ticketRepository::save)
                                                )
                                )
                )
                .flatMap(updated -> createTicketResponseDTO(updated))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<Void> deleteTicket(UUID ticketId) {
        return userRoleUtil.getUserId()
                .flatMap(userId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
                                .flatMap(ticket ->
                                        projectService.getProjectById(ticket.getProjectId())
                                                .flatMap(project -> {

                                                    boolean isAssigner = ticket.getAssignedBy() != null &&
                                                            ticket.getAssignedBy().equals(userId);

                                                    boolean isOwner = project.getOwner() != null &&
                                                            project.getOwner().getId().equals(userId);

                                                    if (!isAssigner && !isOwner) {
                                                        return Mono.error(new RuntimeException(
                                                                "Only the assigner or the project owner can delete this ticket"
                                                        ));
                                                    }

                                                    return ticketRepository.delete(ticket);
                                                })
                                )
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<TicketResponseDTO> getTicketsByProject(UUID projectId) {

        return userRoleUtil.getUserId()
                .flatMapMany(userId ->
                        projectMemberService.isMember(projectId, userId)
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(
                                        new RuntimeException(
                                                "You are not authorized to view tickets of this project"
                                        )
                                ))
                                .thenMany(
                                        ticketRepository.findByProjectId(projectId)
                                                .flatMap(ticket -> createTicketResponseDTO(ticket))
                                )
                );
    }

    @Override
    public Mono<TicketResponseDTO> assignTicket(UUID ticketId, TicketAssignRequestDTO dto) {

        return userRoleUtil.getUserId()
                .flatMap(currentUserId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
                                .flatMap(ticket ->
                                        projectService.getProjectById(ticket.getProjectId())
                                                .filter(project -> {
                                                    boolean isCreator =
                                                            ticket.getCreatedBy() != null &&
                                                                    ticket.getCreatedBy().equals(currentUserId);

                                                    boolean isOwner =
                                                            project.getOwner() != null &&
                                                                    project.getOwner().getId().equals(currentUserId);

                                                    return isCreator || isOwner;
                                                })
                                                .switchIfEmpty(Mono.error(
                                                        new RuntimeException(
                                                                "Only ticket creator or project owner can assign tickets"
                                                        )
                                                ))
                                                .then(
                                                        projectMemberService
                                                                .isMember(ticket.getProjectId(), dto.getAssignedTo())
                                                                .flatMap(isMember -> {
                                                                    if (isMember) {
                                                                        return Mono.empty();
                                                                    }
                                                                    return projectMemberService.addMember(
                                                                            ticket.getProjectId(),
                                                                            dto.getAssignedTo()
                                                                    );
                                                                })
                                                                .then(
                                                                        Mono.fromCallable(() -> {
                                                                            if (ticket.getStatus().equals(TicketStatusEnum.CREATED)) {
                                                                                ticket.setStatus(TicketStatusEnum.ASSIGNED);
                                                                            }
                                                                            ticket.setAssignedTo(dto.getAssignedTo());
                                                                            ticket.setAssignedBy(currentUserId);
                                                                            ticket.setUpdatedAt(LocalDateTime.now());
                                                                            return ticket;
                                                                        }).flatMap(ticketRepository::save)
                                                                )
                                                )
                                )
                )
                .flatMap(this::createTicketResponseDTO)
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<TicketResponseDTO> updateStatus(UUID ticketId, String status) {

        TicketStatusEnum newStatus;
        try {
            newStatus = TicketStatusEnum.fromString(status);
        } catch (IllegalArgumentException e) {
            return Mono.error(new RuntimeException("Invalid ticket status"));
        }

        return userRoleUtil.getUserId()
                .flatMap(currentUserId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
                                .flatMap(ticket ->
                                        userRoleUtil.getPermissions()
                                                .flatMap(permissions -> {
                                                    TicketWorkflow workflow =
                                                            TicketWorkflow.from(ticket.getStatus());

                                                    Set<String> requiredPermissions =
                                                            workflow.validateAndGetPermissions(newStatus);

                                                    if (!permissions.containsAll(requiredPermissions)) {
                                                        return Mono.error(
                                                                new RuntimeException("User not authorized for this action")
                                                        );
                                                    }
                                                    return projectService.getProjectById(ticket.getProjectId())
                                                            .filter(project -> {
                                                                boolean isOwner =
                                                                        project.getOwner() != null &&
                                                                                project.getOwner().getId().equals(currentUserId);

                                                                boolean isCreator =
                                                                        ticket.getCreatedBy() != null &&
                                                                                ticket.getCreatedBy().equals(currentUserId);

                                                                boolean isAssigner =
                                                                        ticket.getAssignedBy() != null &&
                                                                                ticket.getAssignedBy().equals(currentUserId);

                                                                boolean isAssignee =
                                                                        ticket.getAssignedTo() != null &&
                                                                                ticket.getAssignedTo().equals(currentUserId);

                                                                return isOwner || isCreator || isAssigner || isAssignee;
                                                            })
                                                            .switchIfEmpty(Mono.error(new RuntimeException("You are not authorized to update the ticket status")))
                                                            .then(
                                                                    Mono.fromCallable(() -> {
                                                                        ticket.setStatus(newStatus);
                                                                        ticket.setUpdatedAt(LocalDateTime.now());
                                                                        return ticket;
                                                                    }).flatMap(ticketRepository::save)
                                                            );
                                                }))
                )
                .flatMap(saved -> createTicketResponseDTO(saved))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<TicketResponseDTO> getChildTickets(UUID parentTicketId) {

        return userRoleUtil.getUserId()
                .flatMapMany(currentUserId ->
                        ticketRepository.findById(parentTicketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Parent ticket not found")))
                                .flatMapMany(parent ->
                                        projectMemberService.isMember(parent.getProjectId(), currentUserId)
                                                .filter(Boolean::booleanValue)
                                                .switchIfEmpty(Mono.error(
                                                        new RuntimeException(
                                                                "You are not authorized to access these tickets"
                                                        )
                                                ))
                                                .thenMany(
                                                        ticketRepository.findByParentTicket(parentTicketId)
                                                                .flatMap(ticket -> createTicketResponseDTO(ticket))
                                                )
                                )
                );
    }

    @Override
    public Mono<TicketResponseDTO> getBaseTicket(UUID ticketId) {

        return userRoleUtil.getUserId()
                .flatMap(currentUserId ->
                        ticketRepository.findById(ticketId)
                                .switchIfEmpty(Mono.error(new RuntimeException("Ticket not found")))
                                .flatMap(ticket ->
                                        projectMemberService.isMember(ticket.getProjectId(), currentUserId)
                                                .filter(Boolean::booleanValue)
                                                .switchIfEmpty(Mono.error(
                                                        new RuntimeException(
                                                                "You are not authorized to access this ticket"
                                                        )
                                                ))
                                                .then(findBaseTicket(ticket))
                                )
                )
                .flatMap(base -> createTicketResponseDTO(base));
    }

    @Override
    public Flux<TicketResponseDTO> getAssignedTo() {
        return userRoleUtil.getUserId()
                .flatMapMany(user -> ticketRepository.findAllByAssignedTo(user)
                        .flatMap(ticket -> createTicketResponseDTO(ticket)));
    }

    @Override
    public Flux<TicketResponseDTO> getAssignedBy() {
        return userRoleUtil.getUserId()
                .flatMapMany(user -> ticketRepository.findAllByAssignedBy(user)
                        .flatMap(ticket -> createTicketResponseDTO(ticket)));
    }

    //HELPER METHODS
    private Mono<Ticket> findBaseTicket(Ticket ticket) {
        if (ticket.getParentTicket() == null) {
            return Mono.just(ticket);
        }

        return ticketRepository.findById(ticket.getParentTicket())
                .switchIfEmpty(Mono.error(new RuntimeException("Parent ticket not found")))
                .flatMap(this::findBaseTicket);
    }

    private Mono<TicketResponseDTO> createTicketResponseDTO(Ticket ticket) {
        return Mono.zip(
                projectService.getProjectById(ticket.getProjectId()),
                userService.getUserById(ticket.getCreatedBy()),
                Mono.justOrEmpty(ticket.getAssignedBy())
                        .flatMap(userService::getUserById)
                        .onErrorReturn(UserResponse.builder().build())
                        .defaultIfEmpty(UserResponse.builder().build()),
                Mono.justOrEmpty(ticket.getAssignedTo())
                        .flatMap(userService::getUserById)
                        .onErrorReturn(UserResponse.builder().build())
                        .defaultIfEmpty(UserResponse.builder().build())
        ).map(tuple -> {
            ProjectResponseDTO project = tuple.getT1();
            UserResponse createdBy = tuple.getT2();
            UserResponse assignedBy = tuple.getT3();
            UserResponse assignedTo = tuple.getT4();

            return TicketResponseDTO.builder()
                    .id(ticket.getId())
                    .projectId(ticket.getProjectId())
                    .ticketNumber(ticket.getTicketNumber())
                    .ticketCode(project.getProjectCode() + "-" + ticket.getTicketNumber())
                    .title(ticket.getTitle())
                    .description(ticket.getDescription())
                    .status(ticket.getStatus().getLabel())
                    .priority(ticket.getPriority().getLabel())
                    .type(ticket.getType().getLabel())
                    .isBase(ticket.getParentTicket() == null)
                    .parentTicket(ticket.getParentTicket())
                    .createdBy(createdBy)
                    .assignedTo(assignedTo.getId() == null ? null : assignedTo)
                    .assignedBy(assignedBy.getId() == null ? null : assignedBy)
                    .createdAt(ticket.getCreatedAt())
                    .updatedAt(ticket.getUpdatedAt())
                    .build();
        });
    }

    private Mono<ProjectMemberResponseDTO> ensureAssigneeIsProjectMember(UUID projectId, UUID assigneeId) {
        if (assigneeId == null) {
            return Mono.empty();
        }

        return projectMemberService.isMember(projectId, assigneeId)
                .flatMap(isMember -> {
                    if (isMember) {
                        return Mono.empty();
                    }
                    return projectMemberService.addMember(projectId, assigneeId);
                });
    }

}
