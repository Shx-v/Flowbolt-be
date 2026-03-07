package com.shxv.flowbolt.Role.Repository;

import com.shxv.flowbolt.Role.Model.GlobalPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface GlobalPermissionRepository extends ReactiveCrudRepository<GlobalPermission, UUID> {
    Mono<GlobalPermission> findByKey(String key);
    Flux<GlobalPermission> findByKeyInAndActiveTrue(Collection<String> keys);

}
