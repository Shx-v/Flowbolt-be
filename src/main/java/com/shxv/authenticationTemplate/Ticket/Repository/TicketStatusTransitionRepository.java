package com.shxv.authenticationTemplate.Ticket.Repository;

import com.shxv.authenticationTemplate.Ticket.Model.TicketStatusTransition;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TicketStatusTransitionRepository extends ReactiveCrudRepository<TicketStatusTransition, UUID> {

    Mono<TicketStatusTransition> findByFromStatusIdAndToStatusIdAndTicketTypeId(UUID fromStatusId, UUID toStatusId, UUID ticketTypeId);
    Flux<TicketStatusTransition> findAllByFromStatusIdAndTicketTypeId(UUID fromStatusId, UUID ticketTypeId);
}
