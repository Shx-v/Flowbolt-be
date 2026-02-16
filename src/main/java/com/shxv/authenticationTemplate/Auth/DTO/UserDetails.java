package com.shxv.authenticationTemplate.Auth.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.authenticationTemplate.Role.DTO.RoleResponse;
import com.shxv.authenticationTemplate.Role.Model.Permission;
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
public class UserDetails {
    private UUID id;
    private String username;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private RoleResponse role;
    private List<Permission> permissions;
    private String logoPath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
