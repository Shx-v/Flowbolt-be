package com.shxv.authenticationTemplate.Repository;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProjectCountRepositoryCustom {
    Mono<Integer> incrementCount(UUID projectId);
}
