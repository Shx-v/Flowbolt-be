package com.shxv.flowbolt.Role.Controller;

import com.shxv.flowbolt.Role.DTO.RoleRequest;
import com.shxv.flowbolt.Role.DTO.RoleResponse;
import com.shxv.flowbolt.Role.Model.GlobalPermission;
import com.shxv.flowbolt.Role.Service.GlobalPermissionService;
import com.shxv.flowbolt.Role.Service.RoleService;
import com.shxv.flowbolt.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Tag(name = "Role", description = "Role Management APIs")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private GlobalPermissionService globalPermissionService;

    @Operation(summary = "Get a role by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{id}")
    public Mono<ResponseEnvelope<RoleResponse>> getRoleById(@PathVariable UUID id) {
        return roleService.getRoleById(id)
                .map(role -> ResponseEnvelope.<RoleResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Role fetched successfully")
                        .data(role)
                        .build());
    }

    @Operation(summary = "Get all roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    @GetMapping
    public Mono<ResponseEnvelope<List<RoleResponse>>> getAllRoles() {
        return roleService.getAllRole()
                .map(roles -> ResponseEnvelope.<List<RoleResponse>>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("All roles fetched")
                        .data(roles)
                        .build());
    }

    @Operation(summary = "Create a new role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEnvelope<RoleResponse>> createRole(@RequestBody RoleRequest request) {
        return roleService.createRole(request)
                .map(role -> ResponseEnvelope.<RoleResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Role created")
                        .data(role)
                        .build());
    }

    @Operation(summary = "Update a role by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @PutMapping("/{id}")
    public Mono<ResponseEnvelope<RoleResponse>> updateRole(@RequestBody RoleRequest request,
                                                           @PathVariable UUID id) {
        return roleService.updateRole(request, id)
                .map(role -> ResponseEnvelope.<RoleResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Role updated")
                        .data(role)
                        .build());
    }


    @Operation(summary = "Delete a role by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEnvelope<RoleResponse>> deleteRole(@PathVariable UUID id) {
        return roleService.deleteRole(id)
                .map(roleResponse -> ResponseEnvelope.<RoleResponse>builder()
                        .success(true)
                        .status(200)
                        .message("Role deleted successfully")
                        .data(roleResponse)
                        .build());
    }

    @Operation(
            summary = "Get global permission list",
            description = "Retrieves the list of all available global permissions"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Permission list retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GlobalPermission.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @GetMapping("/permission")
    public Mono<ResponseEnvelope<List<GlobalPermission>>> getPermissionList() {
        return globalPermissionService.getPermissionList()
                .collectList()
                .map(perms -> ResponseEnvelope.<List<GlobalPermission>>builder()
                        .success(true)
                        .status(200)
                        .message("Permission list retrieved successfully")
                        .data(perms)
                        .build()
                );
    }

}
