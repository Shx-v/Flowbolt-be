package com.shxv.flowbolt.Dashboard.DTO;

import com.shxv.flowbolt.Ticket.DTO.TicketResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private DashboardSummary summary;

    private TicketDistribution distribution;

    private TicketRiskMetrics riskMetrics;

    private UserWorkloadMetrics workload;

    private List<ProjectHealthMetrics> projectHealth;

    private TicketTrend trends;

    private List<TicketResponse> urgentTickets;
}
