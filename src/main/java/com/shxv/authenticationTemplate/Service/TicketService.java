package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.TicketAssignRequestDTO;
import com.shxv.authenticationTemplate.DTO.TicketRequestDTO;
import com.shxv.authenticationTemplate.DTO.TicketResponseDTO;
import com.shxv.authenticationTemplate.DTO.TicketUpdateRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TicketService {

    Mono<TicketResponseDTO> createTicket(TicketRequestDTO requestDTO);

    Mono<TicketResponseDTO> getTicketById(UUID ticketId);

    Flux<TicketResponseDTO> getAllTickets();

    Mono<TicketResponseDTO> updateTicket(UUID ticketId, TicketUpdateRequestDTO requestDTO);

    Mono<Void> deleteTicket(UUID ticketId);

    Flux<TicketResponseDTO> getTicketsByProject(UUID projectId);

    Mono<TicketResponseDTO> assignTicket(UUID uuid, TicketAssignRequestDTO ticketAssignRequestDTO);

    Mono<TicketResponseDTO> updateStatus(UUID ticketId, String status);

    Flux<TicketResponseDTO> getChildTickets(UUID parentTicketId);

    Mono<TicketResponseDTO> getBaseTicket(UUID ticketId);

    Flux<TicketResponseDTO> getAssignedTo();

    Flux<TicketResponseDTO> getAssignedBy();

}
