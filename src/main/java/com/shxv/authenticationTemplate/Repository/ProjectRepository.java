package com.shxv.authenticationTemplate.Repository;

import com.shxv.authenticationTemplate.Model.Project;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProjectRepository extends ReactiveCrudRepository<Project, UUID> {

}
