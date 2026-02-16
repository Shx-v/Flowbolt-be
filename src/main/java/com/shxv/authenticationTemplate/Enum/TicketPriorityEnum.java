package com.shxv.authenticationTemplate.Enum;

public enum TicketPriorityEnum {

    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
    CRITICAL("Critical", 4);

    private final String label;
    private final int level;

    TicketPriorityEnum(String label, int level) {
        this.label = label;
        this.level = level;
    }

    public String getLabel() {
        return label;
    }

    public int getLevel() {
        return level;
    }

    public static TicketPriorityEnum fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Priority cannot be null or empty");
        }

        for (TicketPriorityEnum priority : values()) {
            if (priority.name().equalsIgnoreCase(value)
                    || priority.getLabel().equalsIgnoreCase(value)) {
                return priority;
            }
        }

        throw new IllegalArgumentException("Invalid priority: " + value);
    }

}
