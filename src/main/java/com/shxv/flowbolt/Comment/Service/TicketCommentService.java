package com.shxv.flowbolt.Comment.Service;

import com.shxv.flowbolt.Comment.DTO.CommentCreate;
import com.shxv.flowbolt.Comment.DTO.CommentResponse;
import com.shxv.flowbolt.Comment.DTO.CommentUpdate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TicketCommentService {

    Mono<CommentResponse> createComment(CommentCreate commentCreate);
    Mono<CommentResponse> updateComment(UUID commentId, CommentUpdate commentUpdate);
    Flux<CommentResponse> getAllCommentsByTicketId(UUID ticketId);

}
