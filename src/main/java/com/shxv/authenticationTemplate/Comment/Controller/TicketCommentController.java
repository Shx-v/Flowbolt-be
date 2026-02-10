package com.shxv.authenticationTemplate.Comment.Controller;

import com.shxv.authenticationTemplate.Comment.DTO.CommentCreate;
import com.shxv.authenticationTemplate.Comment.DTO.CommentResponse;
import com.shxv.authenticationTemplate.Comment.DTO.CommentUpdate;
import com.shxv.authenticationTemplate.Comment.Service.TicketCommentService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/comment")
@Tag(name = "Ticket Comments", description = "APIs for managing ticket comments")
public class TicketCommentController {

    @Autowired
    TicketCommentService ticketCommentService;

    @PostMapping
    @Operation(
            summary = "Create a comment on a ticket",
            description = "Creates a new comment for a ticket. Supports rich content with mentions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Comment created successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid comment payload", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not authorized to comment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    public Mono<ResponseEnvelope<CommentResponse>> createComment(
            @RequestBody CommentCreate commentCreate
    ) {
        return ticketCommentService.createComment(commentCreate)
                .map(createdComment -> ResponseEnvelope.<CommentResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Comment created successfully")
                        .data(createdComment)
                        .build());
    }

    @GetMapping("/ticket/{ticketId}")
    @Operation(
            summary = "Get all comments for a ticket",
            description = "Fetches all active comments associated with a ticket"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    public Mono<ResponseEnvelope<List<CommentResponse>>> getAllCommentsByTicketId(
            @PathVariable("ticketId") UUID ticketId
    ) {
        return ticketCommentService.getAllCommentsByTicketId(ticketId)
                .collectList()
                .map(comments -> ResponseEnvelope.<List<CommentResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Comments retrieved successfully")
                        .data(comments)
                        .build());
    }

    @PutMapping("/{commentId}")
    @Operation(
            summary = "Edit a comment",
            description = "Edits an existing comment. Only the creator or authorized users can edit."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Comment updated successfully",
                    content = @Content(schema = @Schema(implementation = CommentResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid update payload", content = @Content),
            @ApiResponse(responseCode = "403", description = "User not authorized to edit this comment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    public Mono<ResponseEnvelope<CommentResponse>> editComment(
            @PathVariable("commentId") UUID commentId,
            @RequestBody CommentUpdate commentUpdate
    ) {
        return ticketCommentService.updateComment(commentId, commentUpdate)
                .map(updatedComment -> ResponseEnvelope.<CommentResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Comment updated successfully")
                        .data(updatedComment)
                        .build());
    }
}
