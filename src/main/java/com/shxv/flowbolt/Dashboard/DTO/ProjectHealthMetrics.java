package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class ProjectHealthMetrics {
    private UUID projectId;
    private String projectName;
    private String projectCode;
    private Long openTickets;
    private Long overdueTickets;
    private Double healthScore;
}
