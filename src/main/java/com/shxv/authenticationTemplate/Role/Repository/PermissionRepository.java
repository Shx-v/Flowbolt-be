package com.shxv.authenticationTemplate.Role.Repository;

import com.shxv.authenticationTemplate.Role.Model.Permission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PermissionRepository extends ReactiveCrudRepository<Permission, UUID> {
    Mono<Permission> findByName(String name);
}

