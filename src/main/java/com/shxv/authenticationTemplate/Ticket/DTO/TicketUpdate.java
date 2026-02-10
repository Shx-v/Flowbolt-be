package com.shxv.authenticationTemplate.Ticket.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TicketUpdate {

    private String title;
    private String description;
    private LocalDateTime deadline;

}
