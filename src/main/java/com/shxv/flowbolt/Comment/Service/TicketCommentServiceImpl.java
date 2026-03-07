package com.shxv.flowbolt.Comment.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxv.flowbolt.Auth.Service.UserService;
import com.shxv.flowbolt.Auth.Util.UserRoleUtil;
import com.shxv.flowbolt.Comment.DTO.*;
import com.shxv.flowbolt.Comment.Model.TicketComment;
import com.shxv.flowbolt.Comment.Repository.TicketCommentRepository;
import com.shxv.flowbolt.Project.Repository.ProjectRepository;
import com.shxv.flowbolt.ProjectMember.Repository.ProjectMemberRepository;
import com.shxv.flowbolt.ProjectMember.Util.PermissionResolver;
import com.shxv.flowbolt.Ticket.Repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketCommentServiceImpl implements TicketCommentService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    PermissionResolver permissionResolver;

    @Autowired
    TicketCommentRepository ticketCommentRepository;

    @Autowired
    ProjectMemberRepository projectMemberRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserService userService;

    @Override
    public Mono<CommentResponse> createComment(CommentCreate commentCreate) {
        if (commentCreate.getContent() == null || commentCreate.getContent().isEmpty()) {
            return Mono.error(new RuntimeException("Content cannot be empty"));
        }

        for (CommentBlock block : commentCreate.getContent()) {
            if (block instanceof TextBlock text) {
                if (text.getText() == null || text.getText().isBlank()) {
                    return Mono.error(new RuntimeException("Text block cannot be empty"));
                }
            }

            if (block instanceof MentionBlock mention) {
                if (mention.getUserId() == null || mention.getLabel() == null) {
                    return Mono.error(new RuntimeException("Invalid mention block"));
                }
            }
        }

        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> ticketRepository.findById(commentCreate.getTicketId())
                        .switchIfEmpty(Mono.error(new RuntimeException("No ticket found with this ID")))
                        .flatMap(ticket -> {
                            if (!ticket.getActive()) {
                                return Mono.error(new RuntimeException("You cannot comment on an inactive ticket"));
                            }

                            return projectRepository.findById(ticket.getProject())
                                    .switchIfEmpty(Mono.error(new RuntimeException("Invalid project ID in ticket")))
                                    .flatMap(project -> {
                                        Set<UUID> mentionedUserIds = commentCreate.getContent().stream()
                                                .filter(block -> block instanceof MentionBlock)
                                                .map(block -> ((MentionBlock) block).getUserId())
                                                .collect(Collectors.toSet());

                                        return validateMentionedUsers(mentionedUserIds, project.getId())
                                                .then(
                                                        permissionResolver.hasPermission(
                                                                "CREATE_COMMENT",
                                                                adminCheckResponse.getUserId(),
                                                                project.getId()
                                                        )
                                                ).flatMap(isAllowed -> {
                                                    if (
                                                            !adminCheckResponse.getIsAdmin() &&
                                                                    !project.getOwner().equals(adminCheckResponse.getUserId()) &&
                                                                    !isAllowed
                                                    ) {
                                                        return Mono.error(new RuntimeException("You are not allowed to comment here"));
                                                    }

                                                    return Mono.fromCallable(() -> objectMapper.writeValueAsString(commentCreate.getContent()))
                                                            .flatMap(content -> ticketCommentRepository.save(TicketComment.builder()
                                                                            .ticketId(ticket.getId())
                                                                            .content(content)
                                                                            .createdBy(adminCheckResponse.getUserId())
                                                                            .active(true)
                                                                            .createdAt(LocalDateTime.now())
                                                                            .updatedAt(LocalDateTime.now())
                                                                            .build())
                                                                    .flatMap(this::mapToResponse)
                                                            );
                                                });

                                    });
                        })
                );
    }

    @Override
    public Mono<CommentResponse> updateComment(UUID commentId, CommentUpdate commentUpdate) {
        return userRoleUtil.getUserId()
                .flatMap(currentUserId -> ticketCommentRepository.findById(commentId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Invalid comment ID")))
                        .flatMap(ticketComment -> ticketRepository.findById(ticketComment.getTicketId())
                                .switchIfEmpty(Mono.error(new RuntimeException("Invalid ticket ID in comment")))
                                .flatMap(ticket -> {
                                    if (!ticket.getActive()) {
                                        return Mono.error(new RuntimeException("You cannot edit a comment on a ticket which is not active"));
                                    }

                                    if (!currentUserId.equals(ticketComment.getCreatedBy())) {
                                        return Mono.error(new RuntimeException("You cannot edit this comment"));
                                    }

                                    return Mono.fromCallable(() -> objectMapper.writeValueAsString(commentUpdate.getContent()))
                                            .flatMap(content -> {
                                                ticketComment.setContent(content);
                                                ticketComment.setUpdatedAt(LocalDateTime.now());

                                                return ticketCommentRepository.save(ticketComment)
                                                        .flatMap(this::mapToResponse);
                                            });
                                })
                        )
                );
    }

    @Override
    public Flux<CommentResponse> getAllCommentsByTicketId(UUID ticketId) {
        return ticketCommentRepository.findAllByTicketId(ticketId)
                .flatMap(this::mapToResponse);
    }

    //HELPER METHODS
    private Mono<CommentResponse> mapToResponse(TicketComment ticketComment) {
        return Mono.fromCallable(() -> objectMapper.readTree(ticketComment.getContent()))
                .flatMap(content -> userService.getUserById(ticketComment.getCreatedBy())
                        .map(userResponse -> CommentResponse.builder()
                                .id(ticketComment.getId())
                                .ticketId(ticketComment.getTicketId())
                                .content(content)
                                .createdBy(userResponse)
                                .createdAt(ticketComment.getCreatedAt())
                                .updatedAt(ticketComment.getUpdatedAt())
                                .build())
                );
    }

    private Mono<Boolean> isUserAssociated(UUID userId, UUID projectId) {

        return projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
    }

    private Mono<Void> validateMentionedUsers(Set<UUID> userIds, UUID projectId) {
        if (userIds.isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(userIds)
                .flatMap(userId -> isUserAssociated(userId, projectId)
                        .flatMap(isMember -> {
                            if (!isMember) {
                                return Mono.error(new RuntimeException("Mentioned user is not a member of the project"));
                            }
                            return Mono.empty();
                        })
                )
                .then();
    }


}
