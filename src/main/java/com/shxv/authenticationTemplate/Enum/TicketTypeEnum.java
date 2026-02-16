package com.shxv.authenticationTemplate.Enum;

public enum TicketTypeEnum {

    BUG("Bug"),
    FEATURE_REQUEST("Feature Request"),
    TASK("Task"),
    IMPROVEMENT("Improvement"),
    INCIDENT("Incident"),
    SUPPORT("Support");

    private final String label;

    TicketTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static TicketTypeEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Ticket type cannot be null or empty");
        }

        for (TicketTypeEnum type : values()) {
            if (type.name().equalsIgnoreCase(value)
                    || type.getLabel().equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Invalid ticket type: " + value);
    }

}
