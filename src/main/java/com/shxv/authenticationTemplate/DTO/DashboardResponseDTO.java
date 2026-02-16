package com.shxv.authenticationTemplate.DTO;

import com.shxv.authenticationTemplate.Auth.DTO.UserDetails;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponseDTO {
    private UserDetails user;
    private List<ProjectResponseDTO> projects;
    private List<TicketResponseDTO> assignedTo;
    private List<TicketResponseDTO> assignedBy;
}
