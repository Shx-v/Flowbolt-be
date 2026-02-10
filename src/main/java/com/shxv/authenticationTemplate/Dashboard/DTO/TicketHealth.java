package com.shxv.authenticationTemplate.Dashboard.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TicketHealth {
    private Integer open;
    private Integer inProgress;
    private Integer blocked;
    private Integer closed;
}
