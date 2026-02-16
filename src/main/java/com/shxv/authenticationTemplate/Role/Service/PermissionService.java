package com.shxv.authenticationTemplate.Role.Service;

import com.shxv.authenticationTemplate.Role.Enum.PermissionEnum;
import com.shxv.authenticationTemplate.Role.Model.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface PermissionService {
    Mono<Boolean> hasPermission(PermissionEnum permissionEnum);
    Flux<Permission> getAllPermissions(UUID roleId);
    Mono<Boolean> hasUserPermission(UUID uuid, String permission);
}
