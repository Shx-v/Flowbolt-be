package com.shxv.authenticationTemplate.Model;

import com.shxv.authenticationTemplate.Enum.TicketPriorityEnum;
import com.shxv.authenticationTemplate.Enum.TicketStatusEnum;
import com.shxv.authenticationTemplate.Enum.TicketTypeEnum;
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

@Table("tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @Column("id")
    private UUID id;

    @Column("project_id")
    private UUID projectId;

    @Column("ticket_number")
    private Integer ticketNumber;

    @Column("title")
    private String title;

    @Column("description")
    private String description;

    @Column("status")
    private TicketStatusEnum status;

    @Column("priority")
    private TicketPriorityEnum priority;

    @Column("type")
    private TicketTypeEnum type;

    @Column("parent_ticket")
    private UUID parentTicket;

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
