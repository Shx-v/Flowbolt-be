package com.shxv.authenticationTemplate.Ticket.Model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("ticket_status_transitions")
public class TicketStatusTransition {

    @Id
    @Column("id")
    private UUID id;

    @Column("ticket_type_id")
    private UUID ticketTypeId;

    @Column("from_status_id")
    private UUID fromStatusId;

    @Column("to_status_id")
    private UUID toStatusId;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}
