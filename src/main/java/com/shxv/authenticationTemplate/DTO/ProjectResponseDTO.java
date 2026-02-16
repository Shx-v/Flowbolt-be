package com.shxv.authenticationTemplate.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String projectCode;
    private String description;
    private UserResponse createdBy;
    private UserResponse owner;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Ticket> tickets;
    private Boolean isActive;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
