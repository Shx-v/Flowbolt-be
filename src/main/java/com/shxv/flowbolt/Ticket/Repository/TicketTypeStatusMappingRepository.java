package com.shxv.flowbolt.Ticket.Repository;

import com.shxv.flowbolt.Ticket.Model.TicketTypeStatusMapping;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TicketTypeStatusMappingRepository extends ReactiveCrudRepository<TicketTypeStatusMapping, UUID> {

    Mono<TicketTypeStatusMapping> findByTicketTypeIdAndTicketStatusId(UUID ticketTypeId, UUID ticketStatusId);
}
