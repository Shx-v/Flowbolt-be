package com.shxv.authenticationTemplate.Ticket.Repository;

import com.shxv.authenticationTemplate.Ticket.Enum.TicketPriority;
import com.shxv.authenticationTemplate.Ticket.Model.Ticket;
import com.shxv.authenticationTemplate.Ticket.Model.TicketStatusCountProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TicketRepository extends ReactiveCrudRepository<Ticket, UUID> {

    @Query("SELECT MAX(ticket_number) FROM tickets WHERE project = :projectId")
    Mono<Integer> findMaxTicketNumberByProject(UUID projectId);

    @Query("SELECT * FROM tickets WHERE project = :projectId AND active = :active")
    Flux<Ticket> findByProjectAndActive(UUID projectId, Boolean active);

    @Query("SELECT * FROM tickets WHERE active = :active")
    Flux<Ticket> findByActive(Boolean active);

    @Query("SELECT * FROM tickets WHERE project = :projectId")
    Flux<Ticket> findByProject(UUID projectId);

    @Query("SELECT * FROM tickets WHERE id = :id AND active = :active")
    Mono<Ticket> findByIdAndActive(UUID id, Boolean active);

    Flux<Ticket> findAllByParentTicket(UUID parentTicket);

    @Query("""
                SELECT COUNT(*)
                FROM tickets
                WHERE assigned_to = :userId
                  AND active = true
                  AND status != :closedStatusId
            """)
    Mono<Long> countActiveAssignedTickets(
            UUID userId,
            UUID closedStatusId
    );

    @Query("""
                SELECT COUNT(*)
                FROM tickets
                WHERE assigned_to = :userId
                  AND active = true
                  AND status = :status
            """)
    Mono<Integer> countActiveAssignedTicketsAndStatusIs(
            UUID userId,
            UUID status
    );

    Flux<Ticket> findByAssignedToAndPriorityAndActiveTrue(
            UUID assignedTo,
            TicketPriority priority
    );

    Flux<Ticket> findByAssignedToAndStatusAndActiveTrue(
            UUID assignedTo,
            UUID status
    );

    @Query("""
        SELECT *
        FROM tickets
        WHERE deadline IS NOT NULL
          AND deadline < :now
          AND active = true
          AND assigned_to = :userId
    """)
    Flux<Ticket> findOverdueTickets(@Param("now") LocalDateTime now, UUID userId);

}
