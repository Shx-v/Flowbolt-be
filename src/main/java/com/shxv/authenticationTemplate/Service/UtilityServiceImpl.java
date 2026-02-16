package com.shxv.authenticationTemplate.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserDetails;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.DTO.*;
import com.shxv.authenticationTemplate.Enum.TicketPriorityEnum;
import com.shxv.authenticationTemplate.Enum.TicketStatusEnum;
import com.shxv.authenticationTemplate.Enum.TicketTypeEnum;
import com.shxv.authenticationTemplate.Enum.TicketWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Service
public class UtilityServiceImpl implements UtilityService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    UserService userService;

    @Autowired
    ProjectService projectService;

    @Autowired
    TicketService ticketService;

    @Override
    public Mono<DashboardResponseDTO> getDashboardData() {
        return Mono.zip(
                userService.getCurrentUserDetails(),
                projectService.getAllProjects().collectList(),
                ticketService.getAssignedBy().collectList(),
                ticketService.getAssignedTo().collectList()
        ).map(tuple -> {
            UserDetails userDetails = tuple.getT1();
            List<ProjectResponseDTO> projects = tuple.getT2();
            List<TicketResponseDTO> assignedBy = tuple.getT3();
            List<TicketResponseDTO> assignedTo = tuple.getT4();

            return DashboardResponseDTO.builder()
                    .user(userDetails)
                    .projects(projects)
                    .assignedBy(assignedBy)
                    .assignedTo(assignedTo)
                    .build();
        });
    }

    @Override
    public Mono<List<EnumOptionDTO>> getTicketTypes() {
        return Mono.just(Arrays.stream(TicketTypeEnum.values())
                .map(ticketTypeEnum ->
                        EnumOptionDTO.builder()
                                .name(ticketTypeEnum.name())
                                .label(ticketTypeEnum.getLabel())
                                .build())
                .toList());
    }

    @Override
    public Mono<List<EnumOptionDTO>> getTicketPriorities() {
        return Mono.just(Arrays.stream(TicketPriorityEnum.values())
                .map(ticketTypeEnum ->
                        EnumOptionDTO.builder()
                                .name(ticketTypeEnum.name())
                                .label(ticketTypeEnum.getLabel())
                                .build())
                .toList());
    }

    @Override
    public Mono<List<EnumOptionDTO>> getTicketStatus() {
        return Mono.just(Arrays.stream(TicketStatusEnum.values())
                .map(ticketTypeEnum ->
                        EnumOptionDTO.builder()
                                .name(ticketTypeEnum.name())
                                .label(ticketTypeEnum.getLabel())
                                .build())
                .toList());
    }

    @Override
    public Mono<List<AllowedStatusTransition>> getWorkflowByStatus(String status) {
        TicketStatusEnum currentStatus = TicketStatusEnum.fromString(status);
        TicketWorkflow workflow = TicketWorkflow.from(currentStatus);

        return Mono.just(workflow.getTransitions().entrySet()
                .stream()
                .map(entry -> new AllowedStatusTransition(
                        new EnumOptionDTO(
                                entry.getKey().name(),
                                entry.getKey().getLabel()
                        ),
                        entry.getValue().stream().toList()
                ))
                .toList());
    }
}
