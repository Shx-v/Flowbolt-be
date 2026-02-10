package com.shxv.authenticationTemplate.Ticket.Model;

import java.util.UUID;

public interface TicketStatusCountProjection {
    UUID getStatus();
    Long getCount();
}

