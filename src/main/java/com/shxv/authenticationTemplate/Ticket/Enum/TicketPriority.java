package com.shxv.authenticationTemplate.Ticket.Enum;

import lombok.Getter;

@Getter
public enum TicketPriority {

    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int level;

    TicketPriority(int level) {
        this.level = level;
    }

    public static TicketPriority from(String value) {
        if (value == null) {
            throw new RuntimeException("Ticket priority cannot be null");
        }
        try {
            return TicketPriority.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid ticket priority: " + value);
        }
    }
}

