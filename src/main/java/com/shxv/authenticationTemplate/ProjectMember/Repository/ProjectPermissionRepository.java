package com.shxv.authenticationTemplate.ProjectMember.Repository;

import com.shxv.authenticationTemplate.ProjectMember.Model.ProjectPermission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectPermissionRepository extends ReactiveCrudRepository<ProjectPermission, UUID> {

    Mono<ProjectPermission> findByKey(String key);
}
