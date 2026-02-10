package com.shxv.authenticationTemplate.Role.Repository;

import com.shxv.authenticationTemplate.Role.Model.RoleGlobalPermission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface RoleGlobalPermissionRepository extends ReactiveCrudRepository<RoleGlobalPermission, UUID> {

    Flux<RoleGlobalPermission> findAllByRoleId(UUID roleId);

    @Query("SELECT EXISTS (SELECT 1 FROM role_global_permissions WHERE role_id = :roleId AND permission_id = :permissionId)")
    Mono<Boolean> existsByRoleIdAndPermissionId(UUID roleId, UUID permissionId);

    @Query("SELECT permission_id FROM role_global_permissions WHERE role_id = :roleId")
    Flux<UUID> findPermissionIdsByRoleId(UUID roleId);

    @Query("""
                DELETE FROM role_global_permissions
                WHERE role_id = :roleId
                  AND permission_id IN (:permissionIds)
            """)
    Mono<Void> deleteByRoleIdAndPermissionIdIn(
            UUID roleId,
            List<UUID> permissionIds
    );

    @Query("""
                INSERT INTO role_global_permissions (role_id, permission_id)
                VALUES (:roleId, :permissionId)
                ON CONFLICT DO NOTHING
                RETURNING role_id, permission_id
            """)
    Mono<RoleGlobalPermission> insert(UUID roleId, UUID permissionId);

}
