package com.shxv.authenticationTemplate.Role.Controller;

import com.shxv.authenticationTemplate.Role.DTO.RoleRequest;
import com.shxv.authenticationTemplate.Role.DTO.RoleResponse;
import com.shxv.authenticationTemplate.Role.Service.RoleService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEnvelope<Void>> deleteRole(@PathVariable UUID id) {
        return roleService.deleteRole(id)
                .then(Mono.fromSupplier(() -> new ResponseEnvelope<Void>(true, 200, "Role deleted successfully", null)));
    }
}
