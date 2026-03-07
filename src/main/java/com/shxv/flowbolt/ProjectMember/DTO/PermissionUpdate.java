package com.shxv.flowbolt.ProjectMember.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class PermissionUpdate {

    private List<UUID> permissions;
}
