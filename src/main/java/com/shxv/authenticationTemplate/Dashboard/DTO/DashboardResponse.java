package com.shxv.authenticationTemplate.Dashboard.DTO;

import com.shxv.authenticationTemplate.Project.Model.Project;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Actionable actionable;
    private TicketHealth ticketHealth;
    private Load load;
    private List<Project> projects;
//    private List<String> recentEvents;
}
