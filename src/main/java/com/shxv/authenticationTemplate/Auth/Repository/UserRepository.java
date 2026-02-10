package com.shxv.authenticationTemplate.Auth.Repository;

import com.shxv.authenticationTemplate.Auth.Model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);
}
