package com.shxv.authenticationTemplate.Role.Repository;

import com.shxv.authenticationTemplate.Role.Model.RolePermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RolePermissionRepository extends ReactiveCrudRepository<RolePermission, UUID> {

    Flux<RolePermission> findAllByRoleId(UUID roleId);

    @Query("SELECT EXISTS (SELECT 1 FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId)")
    Mono<Boolean> existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    @Query("SELECT permission_id FROM role_permissions WHERE role_id = :roleId")
    Flux<UUID> findPermissionIdsByRoleId(UUID roleId);

}
