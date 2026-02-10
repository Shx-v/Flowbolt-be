package com.shxv.authenticationTemplate.Role.Service;

import com.shxv.authenticationTemplate.Auth.Model.User;
import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Model.GlobalPermission;
import com.shxv.authenticationTemplate.Role.Model.Role;
import com.shxv.authenticationTemplate.Role.Model.RoleGlobalPermission;
import com.shxv.authenticationTemplate.Role.Repository.GlobalPermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RoleGlobalPermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GlobalPermissionServiceImpl implements GlobalPermissionService {

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    GlobalPermissionRepository globalPermissionRepository;

    @Autowired
    RoleGlobalPermissionRepository roleGlobalPermissionRepository;

    @Override
    public Mono<Boolean> hasPermission(GlobalPermission permission) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName())
                .flatMap(username -> userRepository.findByUsername(username))
                .flatMap(user -> {
                    UUID roleId = user.getRole();
                    return globalPermissionRepository.findByKey(permission.getKey())
                            .flatMap(perm -> roleGlobalPermissionRepository.existsByRoleIdAndPermissionId(roleId, perm.getId()));
                });
    }

    @Override
    public Flux<GlobalPermission> getAllPermissions(UUID roleId) {
        return roleGlobalPermissionRepository.findPermissionIdsByRoleId(roleId)
                .flatMap(uuid -> globalPermissionRepository.findById(uuid));
    }

    @Override
    public Mono<Boolean> hasUserPermission(UUID uuid, String permission) {
        return Mono.zip(
                userRepository.findById(uuid),
                globalPermissionRepository.findByKey(permission)
        ).flatMap(tuple -> {
            User user = tuple.getT1();
            GlobalPermission perm = tuple.getT2();

            return roleGlobalPermissionRepository.existsByRoleIdAndPermissionId(user.getRole(), perm.getId());
        });
    }

    @Override
    public Mono<List<GlobalPermission>> validateAndSavePermissions(Role role, List<String> keys) {
        List<String> distinctKeys = keys.stream()
                .map(String::trim)
                .distinct()
                .toList();

        return globalPermissionRepository
                .findByKeyInAndActiveTrue(distinctKeys)
                .collectList()
                .flatMap(globalPermissions -> {

                    if (globalPermissions.size() != distinctKeys.size()) {
                        return Mono.error(
                                new RuntimeException("One or more permissions are invalid or inactive")
                        );
                    }

                    List<RoleGlobalPermission> mappings = globalPermissions.stream()
                            .map(p -> RoleGlobalPermission.builder()
                                    .roleId(role.getId())
                                    .permissionId(p.getId())
                                    .build())
                            .toList();

                    return roleGlobalPermissionRepository
                            .saveAll(mappings)
                            .collectList()
                            .thenReturn(globalPermissions);
                });
    }

    @Override
    public Mono<List<GlobalPermission>> updateRolePermissions(Role role, List<String> requestedKeys) {
        List<String> distinctKeys = requestedKeys.stream()
                .map(String::trim)
                .distinct()
                .toList();

        return globalPermissionRepository
                .findByKeyInAndActiveTrue(distinctKeys)
                .collectList()
                .flatMap(requestedPerms ->
                        getAllPermissions(role.getId())
                                .collectList()
                                .flatMap(existingPerms -> {

                                    Set<String> existingKeys = existingPerms.stream()
                                            .map(GlobalPermission::getKey)
                                            .collect(Collectors.toSet());

                                    Set<String> requestedKeySet = requestedPerms.stream()
                                            .map(GlobalPermission::getKey)
                                            .collect(Collectors.toSet());

                                    List<UUID> toAdd = requestedPerms.stream()
                                            .filter(p -> !existingKeys.contains(p.getKey()))
                                            .map(GlobalPermission::getId)
                                            .toList();

                                    List<UUID> toRemove = existingPerms.stream()
                                            .filter(p -> !requestedKeySet.contains(p.getKey()))
                                            .map(GlobalPermission::getId)
                                            .toList();

                                    return Flux.concat(

                                            /* -------- DELETE FIRST -------- */
                                            toRemove.isEmpty()
                                                    ? Mono.empty()
                                                    : roleGlobalPermissionRepository
                                                    .deleteByRoleIdAndPermissionIdIn(
                                                            role.getId(),
                                                            toRemove
                                                    ),

                                            /* -------- THEN INSERT -------- */
                                            Flux.fromIterable(toAdd)
                                                    .flatMap(permissionId ->
                                                            roleGlobalPermissionRepository.insert(
                                                                    role.getId(),
                                                                    permissionId
                                                            )
                                                    )
                                                    .then()
                                    ).then(Mono.just(requestedPerms));
                                })
                )
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<GlobalPermission> getPermissionList() {
        return globalPermissionRepository.findAll();
    }

}
