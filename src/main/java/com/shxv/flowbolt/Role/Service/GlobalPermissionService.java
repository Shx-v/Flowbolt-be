package com.shxv.flowbolt.Role.Service;

import com.shxv.flowbolt.Role.Model.GlobalPermission;
import com.shxv.flowbolt.Role.Model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GlobalPermissionService {
    Flux<GlobalPermission> getAllPermissions(UUID roleId);
    Mono<List<GlobalPermission>> validateAndSavePermissions(Role role, List<String> keys);
    Mono<List<GlobalPermission>> updateRolePermissions(Role role, List<String> requestedKeys);
    Flux<GlobalPermission> getPermissionList();
}
