package com.shxv.authenticationTemplate.Ticket.Repository;

import com.shxv.authenticationTemplate.Ticket.Model.TicketStatus;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface TicketStatusRepository extends ReactiveCrudRepository<TicketStatus, UUID> {

    Mono<TicketStatus> findByKey(String key);

    @Query("SELECT * FROM ticket_statuses WHERE key = :key")
    Mono<TicketStatus> debugFind(@Param("key") String key);

}
