package com.shxv.flowbolt.Ticket.Repository;

import com.shxv.flowbolt.Ticket.Model.TicketType;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TicketTypeRepository extends ReactiveCrudRepository<TicketType, UUID> {

    Mono<TicketType> findByKey(String key);

}
