package com.shxv.flowbolt.Ticket.Model;

import com.shxv.flowbolt.Ticket.Enum.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tickets")
public class Ticket {

    @Id
    @Column("id")
    private UUID id;

    @Column("project")
    private UUID project;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("ticket_number")
    private Integer ticketNumber;

    @Column("status")
    private UUID status;

    @Column("priority")
    private TicketPriority priority;

    @Column("type")
    private UUID type;

    @Column("parent_ticket")
    private UUID parentTicket;

    @Column("active")
    private Boolean active;

    @Column("deadline")
    private LocalDateTime deadline;

    @Column("completed_at")
    private LocalDateTime completedAt;

    @Column("created_by")
    private UUID createdBy;

    @Column("assigned_to")
    private UUID assignedTo;

    @Column("assigned_by")
    private UUID assignedBy;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
