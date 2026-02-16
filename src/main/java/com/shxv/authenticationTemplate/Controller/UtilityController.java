package com.shxv.authenticationTemplate.Controller;

import com.shxv.authenticationTemplate.DTO.AllowedStatusTransition;
import com.shxv.authenticationTemplate.DTO.DashboardResponseDTO;
import com.shxv.authenticationTemplate.DTO.EnumOptionDTO;
import com.shxv.authenticationTemplate.Service.UtilityService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class UtilityController {

    @Autowired
    UtilityService utilityService;

    @GetMapping("/dashboard")
    public Mono<ResponseEnvelope<DashboardResponseDTO>> getDashboardDta() {
        return utilityService.getDashboardData()
                .map(dashboardResponseDTO -> ResponseEnvelope.<DashboardResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Dashboard data retrieved successfully")
                        .data(dashboardResponseDTO)
                        .build());
    }

    @GetMapping("/ticket-type")
    public Mono<ResponseEnvelope<List<EnumOptionDTO>>> getTicketTypes() {
        return utilityService.getTicketTypes()
                .map(ticketTypes -> ResponseEnvelope.<List<EnumOptionDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket types retrieved successfully")
                        .data(ticketTypes)
                        .build());

    }

    @GetMapping("/ticket-priority")
    public Mono<ResponseEnvelope<List<EnumOptionDTO>>> getTicketPriorities() {
        return utilityService.getTicketPriorities()
                .map(ticketTypes -> ResponseEnvelope.<List<EnumOptionDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket types retrieved successfully")
                        .data(ticketTypes)
                        .build());
    }

    @GetMapping("/ticket-status")
    public Mono<ResponseEnvelope<List<EnumOptionDTO>>> getTicketStatus() {
        return utilityService.getTicketStatus()
                .map(ticketTypes -> ResponseEnvelope.<List<EnumOptionDTO>>builder()
                        .success(true)
                        .status(200)
                        .message("Ticket types retrieved successfully")
                        .data(ticketTypes)
                        .build());
    }

    @GetMapping("/workflow/{status}")
    public Mono<ResponseEnvelope<List<AllowedStatusTransition>>> getWorkflowByStatus(@PathVariable("status") String status) {
        return utilityService.getWorkflowByStatus(status)
                .map(allowedStatusTransitions -> ResponseEnvelope.<List<AllowedStatusTransition>>builder()
                        .success(true)
                        .status(200)
                        .message("Workflow retrieved successfully")
                        .data(allowedStatusTransitions)
                        .build());
    }
}
