package com.shxv.flowbolt.Ticket.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TicketCreate {

    private UUID project;
    private String title;
    private String description;
    private String priority;
    private UUID type;
    private LocalDateTime deadline;
    private UUID parentTicket;
    private UUID assignedTo;

}
