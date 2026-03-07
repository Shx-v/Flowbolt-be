package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Builder
@Getter
@Setter
public class TicketDistribution {
    private Map<String, Long> byStatus;
    private Map<String, Long> byPriority;
    private Map<String, Long> byType;
}
