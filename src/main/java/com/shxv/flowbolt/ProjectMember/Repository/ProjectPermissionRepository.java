package com.shxv.flowbolt.ProjectMember.Repository;

import com.shxv.flowbolt.ProjectMember.Model.ProjectPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectPermissionRepository extends ReactiveCrudRepository<ProjectPermission, UUID> {

    Mono<ProjectPermission> findByKey(String key);
}
