package com.shxv.authenticationTemplate.Role.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.authenticationTemplate.Role.Model.GlobalPermission;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
    private List<GlobalPermission> permissions;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

