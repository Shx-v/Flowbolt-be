package com.shxv.authenticationTemplate.Controller;

import com.shxv.authenticationTemplate.DTO.TicketAssignRequestDTO;
import com.shxv.authenticationTemplate.DTO.TicketRequestDTO;
import com.shxv.authenticationTemplate.DTO.TicketResponseDTO;
import com.shxv.authenticationTemplate.DTO.TicketUpdateRequestDTO;
import com.shxv.authenticationTemplate.Service.TicketService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
@RequiredArgsConstructor
public class TicketController {

    @Autowired
    private final TicketService ticketService;

    @Operation(summary = "Create a new ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ticket created successfully")
    })
    @PostMapping
    public Mono<ResponseEnvelope<TicketResponseDTO>> createTicket(
            @RequestBody TicketRequestDTO requestDTO
    ) {
        return ticketService.createTicket(requestDTO)
                .map(ticket -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(201)
                        .message("Ticket created successfully")
                        .data(ticket)
                        .build());
    }

    @GetMapping
    public Mono<ResponseEnvelope<List<TicketResponseDTO>>> getAllTickets() {
        return ticketService.getAllTickets()
                .collectList()
                .map(tickets -> ResponseEnvelope.<List<TicketResponseDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Tickets retrieved successfully")
                        .data(tickets)
                        .build());
    }

    @Operation(summary = "Get ticket by ID")
    @GetMapping("/{ticketId}")
    public Mono<ResponseEnvelope<TicketResponseDTO>> getTicketById(
            @PathVariable UUID ticketId
    ) {
        return ticketService.getTicketById(ticketId)
                .map(ticket -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket fetched successfully")
                        .data(ticket)
                        .build());
    }

    @Operation(summary = "Get all tickets under a project")
    @GetMapping("/project/{projectId}")
    public Mono<ResponseEnvelope<List<TicketResponseDTO>>> getTicketsByProject(
            @PathVariable UUID projectId
    ) {
        return ticketService.getTicketsByProject(projectId)
                .collectList()
                .map(list -> ResponseEnvelope.<List<TicketResponseDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Tickets fetched successfully")
                        .data(list)
                        .build());
    }

    @Operation(summary = "Update a ticket")
    @PutMapping("/{ticketId}")
    public Mono<ResponseEnvelope<TicketResponseDTO>> updateTicket(
            @PathVariable UUID ticketId,
            @RequestBody TicketUpdateRequestDTO requestDTO
    ) {
        return ticketService.updateTicket(ticketId, requestDTO)
                .map(updated -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket updated successfully")
                        .data(updated)
                        .build());
    }

    @Operation(summary = "Delete a ticket")
    @DeleteMapping("/{ticketId}")
    public Mono<ResponseEnvelope<Void>> deleteTicket(@PathVariable UUID ticketId) {
        return ticketService.deleteTicket(ticketId)
                .then(Mono.just(
                        ResponseEnvelope.<Void>builder()
                                .success(true)
                                .status(200)
                                .message("Ticket deleted successfully")
                                .build()
                ));
    }

    @Operation(summary = "Assign a ticket to a user")
    @PostMapping("/assign/{id}")
    public Mono<ResponseEnvelope<TicketResponseDTO>> assignTicket(
            @RequestBody TicketAssignRequestDTO requestDTO,
            @PathVariable("id") UUID id
    ) {
        return ticketService.assignTicket(id,requestDTO)
                .map(res -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket assigned successfully")
                        .data(res)
                        .build());
    }

    @Operation(summary = "Update ticket status")
    @PatchMapping("/{ticketId}/status/{status}")
    public Mono<ResponseEnvelope<TicketResponseDTO>> updateStatus(
            @PathVariable UUID ticketId,
            @PathVariable String status
    ) {
        return ticketService.updateStatus(ticketId, status)
                .map(ticket -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Status updated successfully")
                        .data(ticket)
                        .build());
    }

    @Operation(summary = "Get all child tickets for a parent ticket")
    @GetMapping("/{ticketId}/children")
    public Mono<ResponseEnvelope<List<TicketResponseDTO>>> getChildTickets(
            @PathVariable UUID ticketId
    ) {
        return ticketService.getChildTickets(ticketId)
                .collectList()
                .map(list -> ResponseEnvelope.<List<TicketResponseDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Child tickets fetched successfully")
                        .data(list)
                        .build());
    }

    @Operation(summary = "Get base ticket of this ticket")
    @GetMapping("/{ticketId}/base")
    public Mono<ResponseEnvelope<TicketResponseDTO>> getBaseTicket(
            @PathVariable UUID ticketId
    ) {
        return ticketService.getBaseTicket(ticketId)
                .map(base -> ResponseEnvelope.<TicketResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Base ticket fetched successfully")
                        .data(base)
                        .build());
    }
}
