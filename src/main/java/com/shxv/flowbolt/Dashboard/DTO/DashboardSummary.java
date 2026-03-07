package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DashboardSummary {
    private Long totalProjects;
    private Long activeProjects;
    private Long totalTickets;
    private Long openTickets;
    private Long overdueTickets;
    private Long blockedTickets;
    private Long highPriorityTickets;
    private Double resolutionRate;
    private Double slaComplianceRate;
}
