package com.shxv.flowbolt.Ticket.Repository;

import com.shxv.flowbolt.Dashboard.DTO.DailyStat;
import com.shxv.flowbolt.Dashboard.DTO.LabelCountProjection;
import com.shxv.flowbolt.Ticket.Enum.TicketPriority;
import com.shxv.flowbolt.Ticket.Model.Ticket;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
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

    Mono<Long> countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Flux<Ticket> findByAssignedTo(UUID assignedTo);

    @Query("""
                SELECT COUNT(t)
                FROM tickets t
                JOIN ticket_statuses ts ON ts.id = t.status
                WHERE ts.terminal = false
            """)
    Mono<Long> countOpenTickets();

    @Query("""
                SELECT COUNT(t)
                FROM tickets t
                JOIN ticket_statuses ts ON ts.id = t.status
                WHERE ts.terminal = false
                AND t.assigned_to = :userId
            """)
    Mono<Long> countOpenTicketsByAssignedTo(UUID userId);

    Mono<Long> countByCompletedAtBetween(LocalDateTime start, LocalDateTime end);

    Mono<Long> countByDeadlineIsNotNullAndCompletedAtIsNotNull();

    @Query("""
                SELECT COUNT(*)
                FROM tickets
                WHERE deadline IS NOT NULL
                  AND completed_at IS NOT NULL
                  AND completed_at <= deadline
            """)
    Mono<Long> countSlaCompliantTickets();

    @Query("""
                SELECT ts.key AS label, COUNT(*) AS count
                FROM tickets t
                JOIN ticket_statuses ts ON ts.id = t.status
                GROUP BY ts.key
            """)
    Flux<LabelCountProjection> countGroupedByStatus();

    @Query("""
                    SELECT ts.key AS label,
                           COUNT(*) AS count
                    FROM tickets t
                    JOIN ticket_statuses ts ON ts.id = t.status
                    WHERE t.assigned_to = :userId
                    GROUP BY ts.key
            """)
    Flux<LabelCountProjection> countByUserGroupedByStatus(UUID userId);

    @Query("""
                SELECT priority AS label, COUNT(*) AS count
                FROM tickets
                GROUP BY priority
            """)
    Flux<LabelCountProjection> countGroupedByPriority();

    @Query("""
                SELECT priority AS label, COUNT(*) AS count
                FROM tickets
                WHERE t.assigned_to = :userId
                GROUP BY priority
            """)
    Flux<LabelCountProjection> countByUserGroupedByPriority(UUID userId);

    @Query("""
                SELECT tt.key AS label, COUNT(*) AS count
                FROM tickets t
                JOIN ticket_types tt ON tt.id = t.type
                GROUP BY tt.key
            """)
    Flux<LabelCountProjection> countGroupedByType();

    @Query("""
                SELECT tt.key AS label, COUNT(*) AS count
                FROM tickets t
                JOIN ticket_types tt ON tt.id = t.type
                WHERE t.assigned_to = :userId
                GROUP BY tt.key
            """)
    Flux<LabelCountProjection> countByUserGroupedByType(UUID userId);

    @Query("""
                SELECT COUNT(*)
                FROM tickets t
                JOIN ticket_statuses ts ON t.status = ts.id
                WHERE t.assigned_to = :userId
                AND ts.terminal = false
            """)
    Mono<Long> countActiveAssignedTo(UUID userId);

    @Query("""
                SELECT COUNT(*)
                FROM tickets
                WHERE created_by = :userId
            """)
    Mono<Long> countCreatedBy(UUID userId);

    @Query("""
                SELECT COUNT(*)
                FROM tickets
                WHERE assigned_to = :userId
                AND completed_at >= :startOfWeek
                AND completed_at < :endOfWeek
            """)
    Mono<Long> countCompletedByThisWeek(UUID userId, LocalDateTime startOfWeek, LocalDateTime endOfWeek);

    @Query("""
                SELECT COALESCE(
                    AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600),
                    0
                )
                FROM tickets
                WHERE assigned_to = :userId
                AND completed_at IS NOT NULL
            """)
    Mono<Double> averageResolutionTimeHours(UUID userId);

    @Query("""
                SELECT DATE(created_at) AS date,
                       COUNT(*) AS count
                FROM tickets
                WHERE created_at >= :startDate
                  AND created_at < :endDate
                GROUP BY DATE(created_at)
                ORDER BY DATE(created_at)
            """)
    Flux<DailyStat> findCreatedTrend(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
                SELECT DATE(completed_at) AS date,
                       COUNT(*) AS count
                FROM tickets
                WHERE completed_at IS NOT NULL
                  AND completed_at >= :startDate
                  AND completed_at < :endDate
                GROUP BY DATE(completed_at)
                ORDER BY DATE(completed_at)
            """)
    Flux<DailyStat> findResolvedTrend(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
                SELECT COUNT(*)
                FROM tickets t
                JOIN ticket_statuses ts ON t.status = ts.id
                WHERE t.project = :projectId
                  AND ts.terminal = false
            """)
    Mono<Long> countOpenByProject(UUID projectId);

    @Query("""
                SELECT COUNT(*)
                FROM tickets t
                JOIN ticket_statuses ts ON t.status = ts.id
                WHERE t.project = :projectId
                  AND ts.terminal = false
                  AND t.deadline IS NOT NULL
                  AND t.deadline < NOW()
            """)
    Mono<Long> countOverdueByProject(UUID projectId);


}
