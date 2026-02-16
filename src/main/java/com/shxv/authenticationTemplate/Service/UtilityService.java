package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.DTO.AllowedStatusTransition;
import com.shxv.authenticationTemplate.DTO.DashboardResponseDTO;
import com.shxv.authenticationTemplate.DTO.EnumOptionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UtilityService {

    Mono<DashboardResponseDTO> getDashboardData();

    Mono<List<EnumOptionDTO>> getTicketTypes();

    Mono<List<EnumOptionDTO>> getTicketPriorities();

    Mono<List<EnumOptionDTO>> getTicketStatus();

    Mono<List<AllowedStatusTransition>> getWorkflowByStatus(String status);

}
