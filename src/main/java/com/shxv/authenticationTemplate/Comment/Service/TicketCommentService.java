package com.shxv.authenticationTemplate.Comment.Service;

import com.shxv.authenticationTemplate.Comment.DTO.CommentCreate;
import com.shxv.authenticationTemplate.Comment.DTO.CommentResponse;
import com.shxv.authenticationTemplate.Comment.DTO.CommentUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TicketCommentService {

    Mono<CommentResponse> createComment(CommentCreate commentCreate);
    Mono<CommentResponse> updateComment(UUID commentId, CommentUpdate commentUpdate);
    Flux<CommentResponse> getAllCommentsByTicketId(UUID ticketId);

}
