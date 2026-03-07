package com.shxv.flowbolt.Role.Service;

import com.shxv.flowbolt.Role.DTO.RoleRequest;
import com.shxv.flowbolt.Role.DTO.RoleResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public interface RoleService {
    Mono<RoleResponse> getRoleById(UUID id);
    Mono<List<RoleResponse>> getAllRole();
    Mono<RoleResponse> createRole(RoleRequest roleRequest);
    Mono<RoleResponse> updateRole(RoleRequest roleRequest, UUID id);
    Mono<RoleResponse> deleteRole(UUID id);
}
