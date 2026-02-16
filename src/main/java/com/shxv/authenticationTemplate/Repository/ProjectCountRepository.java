package com.shxv.authenticationTemplate.Repository;

import com.shxv.authenticationTemplate.Model.ProjectCount;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectCountRepository extends ReactiveCrudRepository<ProjectCount, UUID> {
    Mono<ProjectCount> findByProject(UUID projectId);
}
