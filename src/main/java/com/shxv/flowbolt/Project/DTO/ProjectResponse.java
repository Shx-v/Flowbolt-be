package com.shxv.flowbolt.Project.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import com.shxv.flowbolt.Project.Enum.ProjectStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectResponse {

    private UUID id;
    private String name;
    private String projectCode;
    private String description;
    private UUID createdBy;
    private UUID owner;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
