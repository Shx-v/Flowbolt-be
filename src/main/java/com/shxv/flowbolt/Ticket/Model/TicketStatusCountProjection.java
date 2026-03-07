package com.shxv.flowbolt.Ticket.Model;

import java.util.UUID;

public interface TicketStatusCountProjection {
    UUID getStatus();
    Long getCount();
}

