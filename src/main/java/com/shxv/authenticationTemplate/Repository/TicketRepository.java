package com.shxv.authenticationTemplate.Repository;

import com.shxv.authenticationTemplate.Model.Ticket;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface TicketRepository extends ReactiveCrudRepository<Ticket, UUID> {

    Flux<Ticket> findByProjectId(UUID projectId);

    Flux<Ticket> findByParentTicket(UUID ticketId);

    @Query("SELECT * FROM tickets WHERE assigned_to = :id OR assigned_by = :id OR created_by = :id")
    Flux<Ticket> findAllByAssignedToOrAssignedByOrCreatedBy(UUID id);

    @Query("SELECT * FROM tickets WHERE assigned_to = :id")
    Flux<Ticket> findAllByAssignedTo(UUID id);

    @Query("SELECT * FROM tickets WHERE assigned_by = :id")
    Flux<Ticket> findAllByAssignedBy(UUID id);

}
