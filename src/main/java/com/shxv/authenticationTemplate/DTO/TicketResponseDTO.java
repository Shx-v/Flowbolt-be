package com.shxv.authenticationTemplate.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponseDTO {

    private UUID id;
    private UUID projectId;
    private Integer ticketNumber;
    private String ticketCode;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String type;
    private Boolean isBase;
    private UUID parentTicket;
    private UserResponse createdBy;
    private UserResponse assignedTo;
    private UserResponse assignedBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
