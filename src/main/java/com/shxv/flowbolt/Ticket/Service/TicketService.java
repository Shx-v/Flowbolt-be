package com.shxv.flowbolt.Ticket.Service;

import com.shxv.flowbolt.Dashboard.DTO.TicketTrend;
import com.shxv.flowbolt.Ticket.DTO.*;
import com.shxv.flowbolt.Ticket.Model.TicketType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

public interface TicketService {

    Mono<TicketResponse> createTicket(TicketCreate ticketCreate);

    Flux<TicketResponse> getAllTickets();

    Flux<TicketResponse> getAllTicketsByProject(UUID projectId);

    Mono<TicketResponse> getTicketById(UUID ticketId);

    Mono<TicketResponse> updateTicket(UUID ticketId, TicketUpdate ticketUpdate);

    Mono<TicketResponse> deleteTicket(UUID ticketId);

    Mono<Void> deactivateTicketsByProjectId(UUID projectId);

    Mono<Void> activateTicketsByProjectId(UUID projectId);

    Mono<TicketResponse> assignTicket(AssigneeUpdate assigneeUpdate);

    Mono<TicketResponse> updatePriority(PriorityUpdate priorityUpdate);

    Mono<TicketResponse> updateStatus(StatusUpdate statusUpdate);

    Mono<TicketDetailsResponse> getTicketDetailsById(UUID ticketId);

    Flux<TicketType> getTicketTypes();

    Mono<TransitionResponse> getValidTransitions(String status, String type);

    Mono<Long> getTotalTickets();

    Mono<Long> getOpenTickets();

    Mono<Long> getOverdueTickets();

    Mono<Long> getBlockedTickets();

    Mono<Long> getHighPriorityTickets();

    Mono<Double> getResolutionRate();

    Mono<Double> getSLAComplianceRate();

    Mono<Long> getNearDeadlineCount();

    Mono<Long> getOverDueCount();

    Mono<Long> getAgingCount();

    Mono<Long> getAssignedToMeCount();

    Mono<Long> getCreatedByMeCount();

    Mono<Long> getCompletedByMeThisWeekCount();

    Mono<Double> getAverageResolutionTimeHours();

    Mono<TicketTrend> getTicketTrends();

    Mono<Long> getOpenTicketCountByProject(UUID projectId);

    Mono<Long> getOverdueTicketCountByProject(UUID projectId);

    Mono<Double> getHealthScoreByProject(UUID projectId);

    Mono<Map<String, Long>> getTicketStatusDistribution();

    Mono<Map<String, Long>> getTicketTypeDistribution();

    Mono<Map<String, Long>> getTicketPriorityDistribution();

}
