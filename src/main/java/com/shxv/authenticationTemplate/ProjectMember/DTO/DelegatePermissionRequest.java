package com.shxv.authenticationTemplate.ProjectMember.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class DelegatePermissionRequest {

    private UUID delegatedToUser;
    private List<UUID> permissions;
    private UUID group;
    private UUID project;

}
