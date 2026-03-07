package com.shxv.flowbolt.Role.Service;

import com.shxv.flowbolt.Auth.Util.UserRoleUtil;
import com.shxv.flowbolt.Role.DTO.RoleRequest;
import com.shxv.flowbolt.Role.DTO.RoleResponse;
import com.shxv.flowbolt.Role.Model.GlobalPermission;
import com.shxv.flowbolt.Role.Model.Role;
import com.shxv.flowbolt.Role.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    TransactionalOperator transactionalOperator;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    GlobalPermissionService globalPermissionService;

    @Override
    public Mono<RoleResponse> getRoleById(UUID id) {
        return roleRepository.findById(id)
                .flatMap(this::mapToResponse);
    }

    @Override
    public Mono<List<RoleResponse>> getAllRole() {
        return roleRepository.findAll()
                .flatMap(this::mapToResponse)
                .collectList();
    }

    @Override
    public Mono<RoleResponse> createRole(RoleRequest roleRequest) {

        return roleRepository.save(Role.builder()
                        .name(roleRequest.getName())
                        .description(roleRequest.getDescription())
                        .system(false)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
                )
                .flatMap(role -> globalPermissionService
                        .validateAndSavePermissions(role, roleRequest.getGlobalPermissionKeys())
                        .flatMap(globalPermissions -> mapToResponse(role, globalPermissions)));
    }

    @Override
    public Mono<RoleResponse> updateRole(RoleRequest roleRequest, UUID id) {
        return roleRepository.findById(id)
                .flatMap(existingRole -> {
                    existingRole.setName(roleRequest.getName());
                    existingRole.setDescription(roleRequest.getDescription());
                    existingRole.setUpdatedAt(LocalDateTime.now());
                    return roleRepository.save(existingRole)
                            .flatMap(role -> globalPermissionService.updateRolePermissions(role, roleRequest.getGlobalPermissionKeys())
                                    .flatMap(permissions -> mapToResponse(role, permissions)));
                })
                .as(transactionalOperator::transactional);
    }

    @Override
    public Mono<RoleResponse> deleteRole(UUID id) {
        return roleRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Role not found")))
                .flatMap(role -> {
                    if(role.getSystem()) {
                        return Mono.error(new RuntimeException("System roles can not be deleted"));
                    }

                    return roleRepository.deleteById(id)
                            .thenReturn(role);
                })
                .flatMap(this::mapToResponse);
    }

    //HELPER FUNCTIONS
    private Mono<RoleResponse> mapToResponse(Role role) {
        return globalPermissionService.getAllPermissions(role.getId())
                .collectList()
                .flatMap(permissions -> mapToResponse(role, permissions));
    }

    private Mono<RoleResponse> mapToResponse(Role role, List<GlobalPermission> permissions) {
        return Mono.just(
                RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .permissions(permissions)
                        .createdAt(role.getCreatedAt())
                        .updatedAt(role.getUpdatedAt())
                        .build()
        );
    }

}
