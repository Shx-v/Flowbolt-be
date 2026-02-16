package com.shxv.authenticationTemplate.Enum;

import lombok.Getter;

@Getter
public enum TicketStatusEnum {

    CREATED("Created", 1),
    ASSIGNED("Assigned", 2),
    IN_PROGRESS("In Progress", 3),
    BLOCKED("Blocked", 4),
    IN_REVIEW("In Review", 5),
    DONE("Done", 6),
    REOPENED("Reopened", 7),
    CANCELLED("Cancelled", 8);

    private final String label;
    private final int order;

    TicketStatusEnum(String label, int order) {
        this.label = label;
        this.order = order;
    }

    public static TicketStatusEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        for (TicketStatusEnum status : values()) {
            if (status.name().equalsIgnoreCase(value)
                    || status.getLabel().equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid status: " + value);
    }

}

