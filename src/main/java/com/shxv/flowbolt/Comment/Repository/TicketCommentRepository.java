package com.shxv.flowbolt.Comment.Repository;

import com.shxv.flowbolt.Comment.Model.TicketComment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TicketCommentRepository extends ReactiveCrudRepository<TicketComment, UUID> {

    Flux<TicketComment> findAllByTicketId(UUID ticketId);

}
