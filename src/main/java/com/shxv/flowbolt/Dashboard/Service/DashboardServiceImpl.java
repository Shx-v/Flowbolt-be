package com.shxv.flowbolt.Dashboard.Service;

import com.shxv.flowbolt.Dashboard.DTO.*;
import com.shxv.flowbolt.Project.Service.ProjectService;
import com.shxv.flowbolt.Ticket.Service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    ProjectService projectService;

    @Autowired
    TicketService ticketService;

    @Override
    public Mono<DashboardResponse> getDashboardData() {
        return Mono.zip(
                getDashboardSummary(),
                getTicketDistribution(),
                getTicketRiskMetrics(),
                getUserWorkloadMetrics(),
                getTicketTrend(),
                getProjectHealthMatrics().collectList()
        ).map(tuple -> DashboardResponse.builder()
                .summary(tuple.getT1())
                .distribution(tuple.getT2())
                .riskMetrics(tuple.getT3())
                .workload(tuple.getT4())
                .trends(tuple.getT5())
                .projectHealth(tuple.getT6())
                .build()
        );
    }

    //HELPER METHODS
    private Mono<DashboardSummary> getDashboardSummary() {

        return Mono.zip(
                List.of(
                        projectService.getTotalProjects(),
                        projectService.getActiveProjects(),
                        ticketService.getTotalTickets(),
                        ticketService.getOpenTickets(),
                        ticketService.getOverdueTickets(),
                        ticketService.getBlockedTickets(),
                        ticketService.getHighPriorityTickets(),
                        ticketService.getResolutionRate(),
                        ticketService.getSLAComplianceRate()
                ),
                results -> DashboardSummary.builder()
                        .totalProjects((Long) results[0])
                        .activeProjects((Long) results[1])
                        .totalTickets((Long) results[2])
                        .openTickets((Long) results[3])
                        .overdueTickets((Long) results[4])
                        .blockedTickets((Long) results[5])
                        .highPriorityTickets((Long) results[6])
                        .resolutionRate((Double) results[7])
                        .slaComplianceRate((Double) results[8])
                        .build()
        );
    }

    private Mono<TicketDistribution> getTicketDistribution() {
        return Mono.zip(
                        ticketService.getTicketStatusDistribution(),
                        ticketService.getTicketPriorityDistribution(),
                        ticketService.getTicketTypeDistribution()
                )
                .map(tuple -> TicketDistribution.builder()
                        .byStatus(tuple.getT1())
                        .byPriority(tuple.getT2())
                        .byType(tuple.getT3())
                        .build()
                );
    }

    private Mono<TicketRiskMetrics> getTicketRiskMetrics() {

        Mono<Long> nearDeadlineMono = ticketService.getNearDeadlineCount();
        Mono<Long> overdueMono = ticketService.getOverDueCount();
        Mono<Long> agingMono = ticketService.getAgingCount();

        return Mono.zip(nearDeadlineMono, overdueMono, agingMono)
                .map(tuple -> TicketRiskMetrics.builder()
                        .ticketsNearDeadline(tuple.getT1())
                        .ticketsOverdue(tuple.getT2())
                        .agingTickets(tuple.getT3())
                        .build()
                );
    }

    private Flux<ProjectHealthMetrics> getProjectHealthMatrics() {
        return projectService.getProjectHealthMatrics();
    }

    private Mono<TicketTrend> getTicketTrend() {
        return ticketService.getTicketTrends();
    }

    private Mono<UserWorkloadMetrics> getUserWorkloadMetrics() {
        return Mono.zip(
                ticketService.getAssignedToMeCount(),
                ticketService.getCreatedByMeCount(),
                ticketService.getCompletedByMeThisWeekCount(),
                ticketService.getAverageResolutionTimeHours()
        ).map(tuple -> UserWorkloadMetrics.builder()
                .assignedToMe(tuple.getT1())
                .createdByMe(tuple.getT2())
                .completedByMeThisWeek(tuple.getT3())
                .averageResolutionTimeHours(tuple.getT4())
                .build()
        );
    }

}
