package com.shxv.authenticationTemplate.Enum;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

@Getter
public enum TicketWorkflow {

    CREATED(
            Map.of(
                    TicketStatusEnum.CANCELLED,
                    Set.of("CREATE_TICKET")
            )
    ),

    ASSIGNED(
            Map.of(
                    TicketStatusEnum.IN_PROGRESS,
                    Set.of("UPDATE_TICKET"),

                    TicketStatusEnum.CANCELLED,
                    Set.of("CREATE_TICKET")
            )
    ),

    IN_PROGRESS(
            Map.of(
                    TicketStatusEnum.IN_REVIEW,
                    Set.of("UPDATE_TICKET"),

                    TicketStatusEnum.BLOCKED,
                    Set.of("UPDATE_TICKET")
            )
    ),

    BLOCKED(
            Map.of(
                    TicketStatusEnum.IN_PROGRESS,
                    Set.of("UPDATE_TICKET")
            )
    ),

    IN_REVIEW(
            Map.of(
                    TicketStatusEnum.DONE,
                    Set.of("CREATE_TICKET"),

                    TicketStatusEnum.REOPENED,
                    Set.of("CREATE_TICKET")
            )
    ),

    DONE(
            Map.of(
                    TicketStatusEnum.REOPENED,
                    Set.of("CREATE_TICKET")
            )
    ),

    REOPENED(
            Map.of(
                    TicketStatusEnum.IN_PROGRESS,
                    Set.of("UPDATE_TICKET")
            )
    ),

    CANCELLED(Map.of());

    private final Map<TicketStatusEnum, Set<String>> transitions;

    TicketWorkflow(Map<TicketStatusEnum, Set<String>> transitions) {
        this.transitions = transitions;
    }

    // 🔒 Validate transition exists
    public Set<String> validateAndGetPermissions(
            TicketStatusEnum nextStatus
    ) {

        Set<String> permissions = transitions.get(nextStatus);

        if (permissions == null) {
            throw new IllegalStateException(
                    "Invalid status transition from "
                            + this.name() + " to " + nextStatus
            );
        }

        return permissions;
    }

    public static TicketWorkflow from(
            TicketStatusEnum status
    ) {
        return TicketWorkflow.valueOf(status.name());
    }
}
