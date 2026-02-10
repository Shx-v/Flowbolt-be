package com.shxv.authenticationTemplate.Role.Service;

import com.shxv.authenticationTemplate.Role.Model.GlobalPermission;
import com.shxv.authenticationTemplate.Role.Model.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface GlobalPermissionService {
    Mono<Boolean> hasPermission(GlobalPermission permission);
    Flux<GlobalPermission> getAllPermissions(UUID roleId);
    Mono<Boolean> hasUserPermission(UUID uuid, String permission);
    Mono<List<GlobalPermission>> validateAndSavePermissions(Role role, List<String> keys);
    Mono<List<GlobalPermission>> updateRolePermissions(Role role, List<String> requestedKeys);
    Flux<GlobalPermission> getPermissionList();
}
