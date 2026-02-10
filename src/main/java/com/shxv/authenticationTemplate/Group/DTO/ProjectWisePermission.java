package com.shxv.authenticationTemplate.Group.DTO;

import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectPermission;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ProjectWisePermission {
    private UUID id;
    private String name;
    private String projectCode;
    private UUID owner;
    private ProjectStatus status;
    private List<ProjectPermission> permissions;
    private List<UserPermission> userPermissions;
}
