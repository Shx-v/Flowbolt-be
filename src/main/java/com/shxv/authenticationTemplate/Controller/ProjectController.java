package com.shxv.authenticationTemplate.Controller;

import com.shxv.authenticationTemplate.DTO.ProjectCreateRequestDTO;
import com.shxv.authenticationTemplate.DTO.ProjectResponseDTO;
import com.shxv.authenticationTemplate.Service.ProjectService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @Operation(summary = "Get all projects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully")
    })
    @GetMapping
    public Mono<ResponseEnvelope<List<ProjectResponseDTO>>> getAllProjects() {
        return projectService.getAllProjects()
                .collectList()
                .map(list ->
                        ResponseEnvelope.<List<ProjectResponseDTO>>builder()
                                .success(true)
                                .status(200)
                                .message("Projects fetched successfully")
                                .data(list)
                                .build()
                );
    }

    @Operation(summary = "Get project by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEnvelope<ProjectResponseDTO>> getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(project -> ResponseEnvelope.<ProjectResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Project fetched successfully")
                        .data(project)
                        .build()
                )
                .onErrorResume(e ->
                        Mono.just(
                                ResponseEnvelope.<ProjectResponseDTO>builder()
                                        .success(false)
                                        .status(404)
                                        .message(e.getMessage())
                                        .data(null)
                                        .build()
                        )
                );
    }

    @Operation(summary = "Create a project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully")
    })
    @PostMapping
    public Mono<ResponseEnvelope<ProjectResponseDTO>> createProject(
            @RequestBody ProjectCreateRequestDTO requestDTO
    ) {
        return projectService.createProject(requestDTO)
                .map(res -> ResponseEnvelope.<ProjectResponseDTO>builder()
                        .success(true)
                        .status(201)
                        .message("Project created successfully")
                        .data(res)
                        .build()
                );
    }

    @Operation(summary = "Update project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEnvelope<ProjectResponseDTO>> updateProject(
            @PathVariable UUID id,
            @RequestBody ProjectCreateRequestDTO requestDTO
    ) {
        return projectService.updateProject(id, requestDTO)
                .map(updated -> ResponseEnvelope.<ProjectResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Project updated successfully")
                        .data(updated)
                        .build()
                )
                .onErrorResume(e ->
                        Mono.just(
                                ResponseEnvelope.<ProjectResponseDTO>builder()
                                        .success(false)
                                        .status(404)
                                        .message(e.getMessage())
                                        .data(null)
                                        .build()
                        )
                );
    }

    @Operation(summary = "Delete project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEnvelope<ProjectResponseDTO>> deleteProject(@PathVariable UUID id) {
        return projectService.deleteProject(id)
                .map(deleted -> ResponseEnvelope.<ProjectResponseDTO>builder()
                        .success(true)
                        .status(200)
                        .message("Project queued for deletion successfully")
                        .data(deleted)
                        .build()
                )
                .onErrorResume(e ->
                        Mono.just(
                                ResponseEnvelope.<ProjectResponseDTO>builder()
                                        .success(false)
                                        .status(404)
                                        .message(e.getMessage())
                                        .data(null)
                                        .build()
                        )
                );
    }

}
