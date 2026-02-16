package com.shxv.authenticationTemplate.Role.Service;

import com.shxv.authenticationTemplate.Role.DTO.RoleRequest;
import com.shxv.authenticationTemplate.Role.DTO.RoleResponse;
import com.shxv.authenticationTemplate.Role.Model.Role;
import com.shxv.authenticationTemplate.Role.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Mono<RoleResponse> getRoleById(UUID id) {
        return roleRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<List<RoleResponse>> getAllRole() {
        return roleRepository.findAll()
                .map(this::mapToResponse)
                .collectList();
    }

    @Override
    public Mono<RoleResponse> createRole(RoleRequest roleRequest) {
        Role role = Role.builder()
                .name(roleRequest.getName())
                .description(roleRequest.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return roleRepository.save(role)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<RoleResponse> updateRole(RoleRequest roleRequest, UUID id) {
        return roleRepository.findById(id)
                .flatMap(existingRole -> {
                    existingRole.setName(roleRequest.getName());
                    existingRole.setDescription(roleRequest.getDescription());
                    existingRole.setUpdatedAt(LocalDateTime.now());
                    return roleRepository.save(existingRole);
                })
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> deleteRole(UUID id) {
        return roleRepository.deleteById(id);
    }

    //HELPER FUNCTIONS
    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}
