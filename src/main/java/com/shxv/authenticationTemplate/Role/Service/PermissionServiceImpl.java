package com.shxv.authenticationTemplate.Role.Service;

import com.shxv.authenticationTemplate.Auth.Model.User;
import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Enum.PermissionEnum;
import com.shxv.authenticationTemplate.Role.Model.Permission;
import com.shxv.authenticationTemplate.Role.Repository.PermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RolePermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Override
    public Mono<Boolean> hasPermission(PermissionEnum permissionEnum) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> userRepository.findByUsername(username))
                .flatMap(user -> {
                    UUID roleId = user.getRole();
                    return permissionRepository.findByName(permissionEnum.name())
                            .flatMap(permission -> rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permission.getId()));
                });
    }

    @Override
    public Flux<Permission> getAllPermissions(UUID roleId) {
        return rolePermissionRepository.findPermissionIdsByRoleId(roleId)
                .flatMap(uuid -> permissionRepository.findById(uuid));
    }

    @Override
    public Mono<Boolean> hasUserPermission(UUID uuid, String permission) {
        return Mono.zip(
                userRepository.findById(uuid),
                permissionRepository.findByName(permission)
        ).flatMap(tuple -> {
            User user = tuple.getT1();
            Permission perm = tuple.getT2();

            return rolePermissionRepository.existsByRoleIdAndPermissionId(user.getRole(), perm.getId());
        });
    }

}

