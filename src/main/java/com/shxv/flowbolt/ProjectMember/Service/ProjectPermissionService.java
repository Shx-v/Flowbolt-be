package com.shxv.flowbolt.ProjectMember.Service;

import com.shxv.flowbolt.ProjectMember.DTO.PermissionResponse;
import com.shxv.flowbolt.ProjectMember.DTO.PermissionUpdate;
import com.shxv.flowbolt.ProjectMember.Model.ProjectPermission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProjectPermissionService {

    Flux<ProjectPermission> getAllPermission();
    Mono<ProjectPermission> getProjectPermissionById(UUID id);
}
