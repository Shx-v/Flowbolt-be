package com.shxv.authenticationTemplate.Group.DTO;

import com.shxv.authenticationTemplate.ProjectMember.DTO.PermissionResponse;
import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectPermission;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class UserPermission {
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private List<ProjectPermission> permissions;
}
