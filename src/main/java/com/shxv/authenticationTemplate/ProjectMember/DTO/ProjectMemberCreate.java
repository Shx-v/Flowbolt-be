package com.shxv.authenticationTemplate.ProjectMember.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ProjectMemberCreate {
    private UUID project;
    private UUID member;
    private Boolean isGroup;
    private List<UUID> permissions;
}
