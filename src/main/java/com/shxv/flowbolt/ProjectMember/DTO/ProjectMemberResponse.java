package com.shxv.flowbolt.ProjectMember.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import com.shxv.flowbolt.Project.DTO.ProjectResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMemberResponse {

    private UUID id;
    private ProjectResponse project;
    private UUID member;
    private UserResponse user;
    private Boolean active;
    private List<PermissionResponse> permission;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
