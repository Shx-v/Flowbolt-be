package com.shxv.flowbolt.Ticket.Model;

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
@Table("ticket_type_status_mapping")
public class TicketTypeStatusMapping {

    @Id
    @Column("id")
    private UUID id;

    @Column("ticket_type_id")
    private UUID ticketTypeId;

    @Column("ticket_status_id")
    private UUID ticketStatusId;

    @Column("active")
    private Boolean active;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}
