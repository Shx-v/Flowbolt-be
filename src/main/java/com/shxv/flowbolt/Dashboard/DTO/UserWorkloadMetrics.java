package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserWorkloadMetrics {
    private Long assignedToMe;
    private Long createdByMe;
    private Long completedByMeThisWeek;
    private Double averageResolutionTimeHours;
}
