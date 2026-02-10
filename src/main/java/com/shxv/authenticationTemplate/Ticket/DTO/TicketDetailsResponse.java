package com.shxv.authenticationTemplate.Ticket.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Comment.DTO.CommentResponse;
import com.shxv.authenticationTemplate.Project.Model.Project;
import com.shxv.authenticationTemplate.Ticket.Model.Ticket;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketDetailsResponse {

    private UUID id;
    private Project project;
    private String title;
    private String description;
    private String ticketCode;
    private String status;
    private String priority;
    private String type;
    private Boolean isBase;
    private UUID parentTicket;
    private Boolean active;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    private List<TicketResponse> subTickets;
    private List<CommentResponse> comments;
    private UserResponse createdBy;
    private UserResponse assignedTo;
    private UserResponse assignedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
