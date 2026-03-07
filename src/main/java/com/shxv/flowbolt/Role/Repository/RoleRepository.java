package com.shxv.flowbolt.Role.Repository;

import com.shxv.flowbolt.Role.Model.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface RoleRepository extends ReactiveCrudRepository<Role, UUID> {
    Mono<Role> findByName(String name);
}
