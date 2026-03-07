package com.shxv.flowbolt.ProjectMember.Service;

import com.shxv.flowbolt.ProjectMember.Model.ProjectPermission;
import com.shxv.flowbolt.ProjectMember.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class ProjectPermissionServiceImpl implements ProjectPermissionService {

    @Autowired
    ProjectPermissionRepository projectPermissionRepository;

    @Override
    public Flux<ProjectPermission> getAllPermission() {
        return projectPermissionRepository.findAll();
    }

    @Override
    public Mono<ProjectPermission> getProjectPermissionById(UUID id) {
        return projectPermissionRepository.findById(id);
    }

}
