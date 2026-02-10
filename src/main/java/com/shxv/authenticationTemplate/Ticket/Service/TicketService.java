package com.shxv.authenticationTemplate.Ticket.Service;

import com.shxv.authenticationTemplate.Ticket.DTO.*;
import com.shxv.authenticationTemplate.Ticket.Model.TicketType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TicketService {

    Mono<TicketResponse> createTicket(TicketCreate ticketCreate);
    Flux<TicketResponse> getAllTickets();
    Flux<TicketResponse> getAllTicketsByProject(UUID projectId);
    Mono<TicketResponse> getTicketById(UUID ticketId);
    Mono<TicketResponse> updateTicket(UUID ticketId, TicketUpdate ticketUpdate);
    Mono<TicketResponse> deleteTicket(UUID ticketId);
    Mono<TicketResponse> assignTicket(AssigneeUpdate assigneeUpdate);
    Mono<TicketResponse> updatePriority(PriorityUpdate priorityUpdate);
    Mono<TicketResponse> updateStatus(StatusUpdate statusUpdate);
    Mono<TicketDetailsResponse> getTicketDetailsById(UUID ticketId);
    Flux<TicketType> getTicketTypes();
    Mono<TransitionResponse> getValidTransitions(String status, String type);

}
