package com.shxv.authenticationTemplate.Dashboard.DTO;

import com.shxv.authenticationTemplate.Ticket.DTO.TicketResponse;
import com.shxv.authenticationTemplate.Ticket.Model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actionable {
    private List<Ticket> overdue;
    private List<Ticket> highPriority;
    private List<Ticket> inProgress;
}

