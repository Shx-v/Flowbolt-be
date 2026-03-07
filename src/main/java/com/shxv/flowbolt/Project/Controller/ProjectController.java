package com.shxv.flowbolt.Project.Controller;

import com.shxv.flowbolt.Project.DTO.ProjectCreate;
import com.shxv.flowbolt.Project.DTO.ProjectDetailResponse;
import com.shxv.flowbolt.Project.DTO.ProjectResponse;
import com.shxv.flowbolt.Project.DTO.ProjectUpdate;
import com.shxv.flowbolt.Project.Service.ProjectService;
import com.shxv.flowbolt.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(name = "Projects", description = "Project management APIs")
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    ProjectService projectService;

    @PostMapping
    @Operation(
            summary = "Create a new project",
            description = "Creates a new project and assigns the authenticated user as owner"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "Project code already exists")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> createProject(@RequestBody ProjectCreate projectCreate) {
        return projectService.createProject(projectCreate)
                .map(createdProject -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Project created successfully")
                        .data(createdProject)
                        .build());
    }

    @GetMapping
    @Operation(
            summary = "Get all projects",
            description = "Retrieves all projects accessible to the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProjectResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden"
            )
    })
    public Mono<ResponseEnvelope<List<ProjectResponse>>> getAllProjects() {
        return projectService.getAllProjects()
                .collectList()
                .map(projects -> ResponseEnvelope.<List<ProjectResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Projects retrieved successfully")
                        .data(projects)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves a project by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProjectResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project ID format"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            )
    })
    public Mono<ResponseEnvelope<ProjectResponse>> getProjectById(
            @Parameter(
                    description = "Project UUID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable("id") UUID id
    ) {
        return projectService.getProjectById(id)
                .map(project -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project retrieved successfully")
                        .data(project)
                        .build());
    }

    @Operation(
            summary = "Get project details",
            description = "Fetch detailed information of a project using its UUID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEnvelope.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project ID",
                    content = @Content(
                            mediaType = "application/json"
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/json"
                    )
            )
    })
    @GetMapping("/details/{id}")
    public Mono<ResponseEnvelope<ProjectDetailResponse>> getProjectDetails(
            @Parameter(
                    description = "Project UUID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable("id") UUID id
    ) {
        return projectService.getProjectDetail(id)
                .map(project -> ResponseEnvelope.<ProjectDetailResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project retrieved successfully")
                        .data(project)
                        .build());
    }

    @GetMapping("/code/{projectCode}")
    @Operation(
            summary = "Get project by project code",
            description = "Fetch a project using its unique project code"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project fetched successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> getProjectByCode(
            @Parameter(
                    description = "Unique project code",
                    required = true,
                    example = "FLOWBOLT"
            )
            @PathVariable String projectCode
    ) {
        return projectService.getProjectByCode(projectCode)
                .map(project -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project fetched successfully")
                        .data(project)
                        .build()
                );
    }

    @PutMapping(value = "/{id}")
    @Operation(
            summary = "Update a project",
            description = "Updates project details. Only the project creator is allowed to update the project."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(
                            schema = @Schema(implementation = ProjectResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User not allowed to update this project"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Project code already exists"
            )
    })
    public Mono<ResponseEnvelope<ProjectResponse>> updateProject(
            @Parameter(
                    description = "Project ID",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable("id") UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Project update payload (only provided fields will be updated)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ProjectUpdate.class)
                    )
            )
            @RequestBody ProjectUpdate projectUpdate
    )
    {
        return projectService.updateProject(id, projectUpdate)
                .map(updatedProject -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project updated successfully")
                        .data(updatedProject)
                        .build());
    }

    @PostMapping("/{id}/archive")
    @Operation(
            summary = "Archive a project",
            description = "Archives a project. Archived projects become read-only but remain accessible for viewing."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project archived successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> archiveProject(
            @Parameter(
                    description = "Project ID to archive",
                    required = true,
                    example = "c0a8012e-7f7d-4c3a-9f68-0a2d9b0a1234"
            )
            @PathVariable UUID id
    ) {
        return projectService.archiveProject(id)
                .map(project -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project archived successfully")
                        .data(project)
                        .build());
    }

    @PostMapping("/{id}/suspend")
    @Operation(
            summary = "Suspend a project",
            description = "Suspends a project. Suspended projects are locked due to administrative or system reasons."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project suspended successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> suspendProject(
            @Parameter(
                    description = "Project ID to suspend",
                    required = true,
                    example = "c0a8012e-7f7d-4c3a-9f68-0a2d9b0a1234"
            )
            @PathVariable UUID id
    ) {
        return projectService.suspendProject(id)
                .map(project -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project suspended successfully")
                        .data(project)
                        .build());
    }

    @PostMapping("/{id}/restore")
    @Operation(
            summary = "Restore an archived project",
            description = "Restores an archived project back to active state"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Project restored successfully",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "409", description = "Project is not archived")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> restoreProject(
            @Parameter(
                    description = "Project ID to restore",
                    required = true,
                    example = "c0a8012e-7f7d-4c3a-9f68-0a2d9b0a1234"
            )
            @PathVariable UUID id
    ) {
        return projectService.restoreProject(id)
                .map(project -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project restored successfully")
                        .data(project)
                        .build());
    }


    @PostMapping("/{projectId}/transfer-ownership")
    @Operation(
            summary = "Transfer project ownership",
            description = "Transfers ownership of a project to another user. " +
                    "Only admins or the project creator can perform this action. " +
                    "Project must be in ACTIVE state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ownership transferred successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Not authorized to transfer ownership"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    public Mono<ResponseEnvelope<ProjectResponse>> transferOwnership(
            @Parameter(description = "Project ID", required = true)
            @PathVariable UUID projectId,

            @Parameter(description = "New owner user ID", required = true)
            @RequestParam UUID newOwnerId
    ) {
        return projectService.transferOwnership(projectId, newOwnerId)
                .map(response -> ResponseEnvelope.<ProjectResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Project ownership transferred successfully")
                        .data(response)
                        .build());
    }

}
