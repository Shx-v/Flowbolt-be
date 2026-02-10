package com.shxv.authenticationTemplate.Auth.Controller;

import com.shxv.authenticationTemplate.Auth.DTO.UserDetails;
import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @Tag(name = "Users", description = "User management APIs")
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Fetches all users in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Mono<ResponseEnvelope<List<UserResponse>>> getAllUsers() {
        return userService.getAllUsers()
                .collectList()
                .map(userResponses ->
                        ResponseEnvelope.<List<UserResponse>>builder()
                                .success(true)
                                .status(HttpStatus.OK.value())
                                .message("Users fetched successfully")
                                .data(userResponses)
                                .build()
                );
    }

    @Tag(name = "Users", description = "User management APIs")
    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Fetches a user by their unique ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Mono<ResponseEnvelope<UserResponse>> getUserById(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(user -> ResponseEnvelope.<UserResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("User fetched successfully")
                        .data(user)
                        .build()
                );
    }

    @Tag(name = "Users", description = "User management APIs")
    @GetMapping("/detail")
    @Operation(
            summary = "Get current user details",
            description = "Fetches details of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User details fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserDetails.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Mono<ResponseEnvelope<UserDetails>> getUserDetails() {
        return userService.getCurrentUserDetails()
                .map(userDetails -> ResponseEnvelope.<UserDetails>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("User Details fetched successfully")
                        .data(userDetails)
                        .build());
    }

    @Tag(name = "Users", description = "User management APIs")
    @GetMapping("/list")
    @Operation(
            summary = "Get user list",
            description = "Fetches a lightweight list of users for dropdowns or selections"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users fetched successfully",
                    content = @Content(
                            schema = @Schema(implementation = UserListResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Mono<ResponseEnvelope<List<UserListResponse>>> getUserList() {
        return userService.getUserList()
                .collectList()
                .map(userListResponses -> ResponseEnvelope.<List<UserListResponse>>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Users fetched successfully")
                        .data(userListResponses)
                        .build());
    }

}
