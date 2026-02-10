package com.shxv.authenticationTemplate.Comment.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Comment.DTO.*;
import com.shxv.authenticationTemplate.Comment.Model.TicketComment;
import com.shxv.authenticationTemplate.Comment.Repository.TicketCommentRepository;
import com.shxv.authenticationTemplate.Group.Repository.GroupMemberRepository;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectGroupMemberRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectUserMemberRepository;
import com.shxv.authenticationTemplate.ProjectMember.Util.PermissionResolver;
import com.shxv.authenticationTemplate.Ticket.Repository.TicketRepository;
import org.springframework.asm.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
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
    ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    ProjectGroupMemberRepository projectGroupMemberRepository;

    @Autowired
    GroupMemberRepository groupMemberRepository;

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

        Mono<Boolean> directMember = projectUserMemberRepository.existsByProjectIdAndUserId(projectId, userId);

        Mono<Boolean> groupMember = projectGroupMemberRepository.findGroupIdsByProjectId(projectId)
                .flatMap(groupId -> groupMemberRepository.existsByGroupAndMember(groupId, userId))
                .any(Boolean::booleanValue);

        return Mono.zip(directMember, groupMember)
                .map(tuple -> tuple.getT1() || tuple.getT2());
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
