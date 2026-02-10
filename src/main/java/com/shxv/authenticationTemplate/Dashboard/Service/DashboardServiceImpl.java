package com.shxv.authenticationTemplate.Dashboard.Service;

import com.shxv.authenticationTemplate.Auth.DTO.AdminCheckResponse;
import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import com.shxv.authenticationTemplate.Dashboard.DTO.Actionable;
import com.shxv.authenticationTemplate.Dashboard.DTO.DashboardResponse;
import com.shxv.authenticationTemplate.Dashboard.DTO.Load;
import com.shxv.authenticationTemplate.Dashboard.DTO.TicketHealth;
import com.shxv.authenticationTemplate.Project.DTO.ProjectResponse;
import com.shxv.authenticationTemplate.Project.Model.Project;
import com.shxv.authenticationTemplate.Project.Repository.ProjectRepository;
import com.shxv.authenticationTemplate.ProjectMember.Repository.ProjectUserMemberRepository;
import com.shxv.authenticationTemplate.Ticket.Enum.TicketPriority;
import com.shxv.authenticationTemplate.Ticket.Model.TicketStatus;
import com.shxv.authenticationTemplate.Ticket.Model.TicketStatusCountProjection;
import com.shxv.authenticationTemplate.Ticket.Repository.TicketRepository;
import com.shxv.authenticationTemplate.Ticket.Repository.TicketStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TicketStatusRepository ticketStatusRepository;

    @Autowired
    ProjectUserMemberRepository projectUserMemberRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Override
    public Mono<DashboardResponse> getDashboardData() {
        return userRoleUtil.isAdmin()
                .flatMap(adminCheckResponse -> Mono.zip(
                                        getActionable(adminCheckResponse),
                                        getLoad(adminCheckResponse),
                                        getTicketHealth(adminCheckResponse),
                                        getProjects(adminCheckResponse).collectList()
                                )
                                .map(tuple -> DashboardResponse.builder()
                                        .actionable(tuple.getT1())
                                        .load(tuple.getT2())
                                        .ticketHealth(tuple.getT3())
                                        .projects(tuple.getT4())
                                        .build()
                                )
                );
    }

    private Mono<Actionable> getActionable(AdminCheckResponse adminCheckResponse) {
        return Mono.zip(
                ticketRepository.findByAssignedToAndPriorityAndActiveTrue(adminCheckResponse.getUserId(), TicketPriority.CRITICAL).collectList(),
                ticketStatusRepository.findByKey("IN PROGRESS")
                                .flatMapMany(ticketStatus -> ticketRepository.findByAssignedToAndStatusAndActiveTrue(
                                        adminCheckResponse.getUserId(),
                                        ticketStatus.getId())
                                ).collectList(),
                ticketRepository.findOverdueTickets(LocalDateTime.now(), adminCheckResponse.getUserId()).collectList()

        ).map(tuple -> Actionable.builder()
                .highPriority(tuple.getT1())
                .inProgress(tuple.getT2())
                .overdue(tuple.getT3())
                .build()
        );
    }

    private Mono<Load> getLoad(AdminCheckResponse adminCheckResponse) {
        if (adminCheckResponse.getIsAdmin()) {
            return Mono.just(
                    Load.builder()
                            .assignedCount(0)
                            .capacityPercent(0)
                            .build()
            );
        }

        return ticketStatusRepository.findByKey("CLOSED")
                .flatMap(ticketStatus -> ticketRepository.countActiveAssignedTickets(adminCheckResponse.getUserId(), ticketStatus.getId())
                        .map(aLong -> Load.builder()
                                .assignedCount(aLong.intValue())
                                .capacityPercent(calculateCapacityPercent(aLong))
                                .build()
                        )
                );
    }

    private Mono<TicketHealth> getTicketHealth(AdminCheckResponse adminCheckResponse) {

        Mono<TicketStatus> open$ = ticketStatusRepository.findByKey("OPEN");
        Mono<TicketStatus> inProgress$ = ticketStatusRepository.findByKey("IN PROGRESS");
        Mono<TicketStatus> blocked$ = ticketStatusRepository.findByKey("BLOCKED");
        Mono<TicketStatus> closed$ = ticketStatusRepository.findByKey("CLOSED");

        return Mono.zip(open$, inProgress$, blocked$, closed$)
                .switchIfEmpty(Mono.error(
                        new IllegalStateException("One or more ticket statuses missing")
                ))
                .flatMap(statusTuple ->
                        Mono.zip(
                                        ticketRepository.countActiveAssignedTicketsAndStatusIs(adminCheckResponse.getUserId(), statusTuple.getT1().getId()),
                                        ticketRepository.countActiveAssignedTicketsAndStatusIs(adminCheckResponse.getUserId(), statusTuple.getT2().getId()),
                                        ticketRepository.countActiveAssignedTicketsAndStatusIs(adminCheckResponse.getUserId(), statusTuple.getT3().getId()),
                                        ticketRepository.countActiveAssignedTicketsAndStatusIs(adminCheckResponse.getUserId(), statusTuple.getT4().getId())
                                )
                                .map(tuple -> TicketHealth.builder()
                                        .open(tuple.getT1())
                                        .inProgress(tuple.getT2())
                                        .blocked(tuple.getT3())
                                        .closed(tuple.getT4())
                                        .build()
                                )
                );
    }

    private Flux<Project> getProjects(AdminCheckResponse adminCheckResponse) {
        return projectUserMemberRepository.findAllByUserId(adminCheckResponse.getUserId())
                .flatMap(projectUserMember -> projectRepository.findById(projectUserMember.getProjectId()));
    }

    private int calculateCapacityPercent(long assignedCount) {
        int MAX_CAPACITY = 15;
        return Math.min(100, (int) ((assignedCount * 100) / MAX_CAPACITY));
    }

    private Integer getCount(Map<UUID, Long> map, UUID statusId) {
        Long value = map.get(statusId);
        return value == null ? 0 : value.intValue();
    }

}
