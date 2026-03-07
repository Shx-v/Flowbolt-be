package com.shxv.flowbolt.ProjectMember.Controller;

import com.shxv.flowbolt.Auth.DTO.UserListResponse;
import com.shxv.flowbolt.ProjectMember.DTO.*;
import com.shxv.flowbolt.ProjectMember.Model.ProjectPermission;
import com.shxv.flowbolt.ProjectMember.Service.ProjectMemberService;
import com.shxv.flowbolt.ProjectMember.Service.ProjectPermissionService;
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

@Tag(
        name = "Project Members",
        description = "Manage project members (users & groups) and their permissions"
)
@RestController
@RequestMapping("/project-member")
public class ProjectMemberController {

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ProjectPermissionService projectPermissionService;

    @Operation(
            summary = "Get all project permissions",
            description = "Fetches all available project-level permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All permissions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseEnvelope.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/permissions")
    public Mono<ResponseEnvelope<List<ProjectPermission>>> getAllProjectPermissions() {
        return projectPermissionService.getAllPermission()
                .collectList()
                .map(perms -> ResponseEnvelope.<List<ProjectPermission>>builder()
                        .success(true)
                        .status(200)
                        .message("All permissions retrieved successfully")
                        .data(perms)
                        .build()
                );
    }

    @Operation(
            summary = "Add a user or group to a project",
            description = """
                    Adds a member to a project.
                    
                    - Member can be either a **user** or a **group**
                    - Initial permissions can be assigned at creation
                    - Only project owner or admin can perform this action
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Member added successfully"),
            @ApiResponse(responseCode = "403", description = "Not authorized to add members"),
            @ApiResponse(responseCode = "404", description = "Project / user / group not found")
    })
    @PostMapping
    public Mono<ResponseEnvelope<ProjectMemberResponse>> addMemberToProject(@RequestBody ProjectMemberCreate projectMemberCreate) {
        return projectMemberService.createProjectMember(projectMemberCreate)
                .map(addedMember -> ResponseEnvelope.<ProjectMemberResponse>builder()
                        .success(true)
                        .status(201)
                        .message("Member added successfully")
                        .data(addedMember)
                        .build());
    }

    @Operation(
            summary = "Get all members of a project",
            description = """
                    Returns all members of a project.
                    
                    - Includes both **user members** and **group members**
                    - Each member includes assigned permissions
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Project members retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{projectId}")
    public Mono<ResponseEnvelope<List<ProjectMemberResponse>>> getProjectMembers(
            @Parameter(description = "Project ID", required = true)
            @PathVariable UUID projectId
    ) {
        return projectMemberService.getProjectMembers(projectId)
                .collectList()
                .map(projectMemberResponses -> ResponseEnvelope.<List<ProjectMemberResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Project members retrieved successfully")
                        .data(projectMemberResponses)
                        .build());
    }

    @Operation(
            summary = "Get a specific project member",
            description = """
                    Fetch a project member by **memberId**.
                    
                    - `memberId` can be either a **userId** or a **groupId**
                    - The system automatically resolves the correct member type
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found in project")
    })
    @GetMapping("/{projectId}/member/{memberId}")
    public Mono<ResponseEnvelope<ProjectMemberResponse>> getProjectMember(
            @Parameter(description = "Project ID", required = true)
            @PathVariable UUID projectId,

            @Parameter(description = "User ID or Group ID", required = true)
            @PathVariable UUID memberId
    ) {
        return projectMemberService.getProjectMember(projectId, memberId)
                .map(projectMemberResponse -> ResponseEnvelope.<ProjectMemberResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Member retrieved successfully")
                        .data(projectMemberResponse)
                        .build());
    }

    @Operation(
            summary = "Update project member permissions",
            description = """
                    Replaces all permissions for a project member.
                    
                    - Existing permissions are removed
                    - New permissions are assigned atomically
                    - Works for both **user members** and **group members**
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project member not found")
    })
    @PutMapping("/{projectMemberId}")
    public Mono<ResponseEnvelope<List<PermissionResponse>>> updatePermissions(
            @Parameter(description = "Project member ID", required = true)
            @PathVariable UUID projectMemberId,

            @RequestBody PermissionUpdate permissionUpdate
    ) {
        return projectMemberService.updateMemberPermissions(
                        projectMemberId,
                        permissionUpdate
                )
                .map(permissionResponses -> ResponseEnvelope.<List<PermissionResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Permissions updated successfully")
                        .data(permissionResponses)
                        .build());
    }

    @Operation(
            summary = "Remove a project member",
            description = """
                    Permanently removes a member from the project.
                    
                    - This is a **hard delete**
                    - All associated permissions are deleted via cascade
                    - Action is irreversible
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "404", description = "Project member not found")
    })
    @DeleteMapping("/{projectMemberId}")
    public Mono<ResponseEnvelope<ProjectMemberResponse>> removeMember(
            @Parameter(description = "Project member ID", required = true)
            @PathVariable UUID projectMemberId
    ) {
        return projectMemberService.removeMember(projectMemberId)
                .map(projectMemberResponse -> ResponseEnvelope.<ProjectMemberResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Member removed successfully")
                        .data(projectMemberResponse)
                        .build());
    }

    @Operation(
            summary = "Get project members",
            description = "Returns all users who are members of the given project. " +
                    "This includes direct project members and users who belong to groups added to the project."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ResponseEnvelope.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid project ID supplied"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Project not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("/members/{projectId}")
    public Mono<ResponseEnvelope<List<UserListResponse>>> getProjectMemberUsers(
            @Parameter(
                    description = "UUID of the project whose members need to be fetched",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID projectId
    ) {
        return projectMemberService.getMemberUsers(projectId)
                .collectList()
                .map(users -> ResponseEnvelope.<List<UserListResponse>>builder()
                        .success(true)
                        .status(200)
                        .message("Members retrieved successfully")
                        .data(users)
                        .build()
                );
    }


}
