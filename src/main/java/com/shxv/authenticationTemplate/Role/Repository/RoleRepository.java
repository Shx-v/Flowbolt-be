package com.shxv.authenticationTemplate.Role.Repository;

import com.shxv.authenticationTemplate.Role.Model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, UUID> {
    Mono<Role> findByName(String name);
}
