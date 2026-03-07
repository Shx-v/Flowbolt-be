package com.shxv.flowbolt.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TicketRiskMetrics {
    private Long ticketsNearDeadline;
    private Long ticketsOverdue;
    private Long agingTickets;
}
