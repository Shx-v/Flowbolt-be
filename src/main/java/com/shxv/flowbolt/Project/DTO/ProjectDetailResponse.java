package com.shxv.flowbolt.Project.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import com.shxv.flowbolt.Ticket.DTO.TicketResponse;
import com.shxv.flowbolt.Ticket.Model.Ticket;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectDetailResponse {
    private UUID id;
    private String name;
    private String projectCode;
    private String description;
    private UserResponse createdBy;
    private UserResponse owner;
    private String status;
    private List<TicketResponse> tickets;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
