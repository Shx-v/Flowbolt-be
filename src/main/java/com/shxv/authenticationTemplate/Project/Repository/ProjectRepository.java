package com.shxv.authenticationTemplate.Project.Repository;

import com.shxv.authenticationTemplate.Project.Enum.ProjectStatus;
import com.shxv.authenticationTemplate.Project.Model.Project;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ProjectRepository extends ReactiveCrudRepository<Project, UUID> {

    Flux<Project> findAllByStatusNot(ProjectStatus status);

    Mono<Project> findByProjectCode(String projectCode);

    Flux<Project> findAllByOwner(UUID ownerId);

    Flux<Project> findAllByOwnerAndStatusNot(UUID ownerId, ProjectStatus status);
}

