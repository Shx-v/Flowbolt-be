package com.shxv.flowbolt.Ticket.Controller;

import com.shxv.flowbolt.Ticket.DTO.*;
import com.shxv.flowbolt.Ticket.Model.TicketType;
import com.shxv.flowbolt.Ticket.Service.TicketService;
import com.shxv.flowbolt.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ticket")
@Tag(name = "Ticket", description = "Ticket management APIs")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Operation(
            summary = "Create a new ticket",
            description = "Creates a new ticket in a project"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ticket created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "User not authorized to create ticket")
    })
    @PostMapping
    public Mono<ResponseEnvelope<TicketResponse>> createTicket(
            @RequestBody TicketCreate ticketCreate
    ) {
        return ticketService.createTicket(ticketCreate)
                .map(createdTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Ticket created successfully")
                        .data(createdTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Get all tickets",
            description = "Retrieve a list of all tickets in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tickets retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseEnvelope.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public Mono<ResponseEnvelope<List<TicketResponse>>> getAllTickets() {
        return ticketService.getAllTickets()
                .collectList()
                .map(ticketResponses -> ResponseEnvelope.<List<TicketResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Tickets retrieved successfully")
                        .data(ticketResponses)
                        .build()
                );
    }


    @Operation(
            summary = "Get all tickets by project",
            description = "Fetches all tickets for a given project"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to view tickets"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/project/{projectId}")
    public Mono<ResponseEnvelope<List<TicketResponse>>> getAllTicketsByProject(
            @Parameter(description = "Project ID", required = true)
            @PathVariable UUID projectId
    ) {
        return ticketService.getAllTicketsByProject(projectId)
                .collectList()
                .map(ticketResponses -> ResponseEnvelope.<List<TicketResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Tickets retrieved successfully")
                        .data(ticketResponses)
                        .build()
                );
    }

    @Operation(
            summary = "Get ticket by ID",
            description = "Fetch a single ticket using ticket ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to view ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/{ticketId}")
    public Mono<ResponseEnvelope<TicketResponse>> getTicketById(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable UUID ticketId
    ) {
        return ticketService.getTicketById(ticketId)
                .map(ticketResponse -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket retrieved successfully")
                        .data(ticketResponse)
                        .build()
                );
    }

    @Operation(
            summary = "Update ticket details",
            description = "Updates title and/or description of a ticket"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid update request"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PutMapping("/{ticketId}")
    public Mono<ResponseEnvelope<TicketResponse>> updateTicket(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable UUID ticketId,
            @RequestBody TicketUpdate ticketUpdate
    ) {
        return ticketService.updateTicket(ticketId, ticketUpdate)
                .map(updatedTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket updated successfully")
                        .data(updatedTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Delete a ticket",
            description = "Soft deletes a ticket (marks it inactive)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to delete ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @DeleteMapping("/{ticketId}")
    public Mono<ResponseEnvelope<TicketResponse>> deleteTicket(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable UUID ticketId
    ) {
        return ticketService.deleteTicket(ticketId)
                .map(deletedTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket deleted successfully")
                        .data(deletedTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Assign ticket",
            description = "Assigns a ticket to a user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid assignee"),
            @ApiResponse(responseCode = "403", description = "User not authorized to assign ticket"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/assignee")
    public Mono<ResponseEnvelope<TicketResponse>> assignTicket(
            @RequestBody AssigneeUpdate assigneeUpdate
    ) {
        return ticketService.assignTicket(assigneeUpdate)
                .map(assignedTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket assigned successfully")
                        .data(assignedTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Update ticket priority",
            description = "Updates the priority of a ticket"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket priority updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid priority"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update priority"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/priority")
    public Mono<ResponseEnvelope<TicketResponse>> updatePriority(
            @RequestBody PriorityUpdate priorityUpdate
    ) {
        return ticketService.updatePriority(priorityUpdate)
                .map(updatedTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket priority updated successfully")
                        .data(updatedTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Update ticket status",
            description = "Updates the ticket status following workflow and ownership rules"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or transition"),
            @ApiResponse(responseCode = "403", description = "User not authorized to update status"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PatchMapping("/status")
    public Mono<ResponseEnvelope<TicketResponse>> updateStatus(
            @RequestBody StatusUpdate statusUpdate
    ) {
        return ticketService.updateStatus(statusUpdate)
                .map(updatedTicket -> ResponseEnvelope.<TicketResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket status updated successfully")
                        .data(updatedTicket)
                        .build()
                );
    }

    @Operation(
            summary = "Get detailed ticket information",
            description = "Fetches complete ticket details including extended metadata"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ticket details retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User not authorized to view ticket details"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @GetMapping("/details/{ticketId}")
    public Mono<ResponseEnvelope<TicketDetailsResponse>> getTicketDetailsById(
            @Parameter(description = "Ticket ID", required = true)
            @PathVariable("ticketId") UUID ticketId
    ) {
        return ticketService.getTicketDetailsById(ticketId)
                .map(ticketDetailsResponse -> ResponseEnvelope.<TicketDetailsResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket details retrieved successfully")
                        .data(ticketDetailsResponse)
                        .build());
    }

    @Operation(
            summary = "Get ticket types",
            description = "Retrieve all available ticket types"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Ticket types fetched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEnvelope.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/types")
    public Mono<ResponseEnvelope<List<TicketType>>> getTicketTypes() {
        return ticketService.getTicketTypes()
                .collectList()
                .map(ticketTypes -> ResponseEnvelope.<List<TicketType>>builder()
                        .success(true)
                        .status(200)
                        .message("Types fetched successfully")
                        .data(ticketTypes)
                        .build()
                );
    }

    @Operation(
            summary = "Get next valid ticket status transitions",
            description = "Returns valid next status transitions for a ticket based on ticket type and current status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Valid transitions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransitionResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid ticket type or status"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No valid transitions found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("/status-transitions/{ticketType}/{currentStatus}")
    public Mono<ResponseEnvelope<TransitionResponse>> getNextStatusTransitions(
            @Parameter(
                    description = "Ticket type identifier (e.g. BUG, TASK, INCIDENT)",
                    required = true,
                    example = "BUG"
            )
            @PathVariable String ticketType,

            @Parameter(
                    description = "Current ticket status (e.g. OPEN, IN_PROGRESS, RESOLVED)",
                    required = true,
                    example = "OPEN"
            )
            @PathVariable String currentStatus
    ) {
        return ticketService.getValidTransitions(currentStatus, ticketType)
                .map(response -> ResponseEnvelope.<TransitionResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Valid transitions retrieved successfully")
                        .data(response)
                        .build());
    }

}
