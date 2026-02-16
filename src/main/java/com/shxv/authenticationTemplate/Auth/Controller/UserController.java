package com.shxv.authenticationTemplate.Auth.Controller;

import com.shxv.authenticationTemplate.Auth.DTO.UserDetails;
import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Service.UserService;
import com.shxv.authenticationTemplate.Util.ResponseEnvelope;
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

    @GetMapping
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

    @GetMapping("/{id}")
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

    @GetMapping("/detail")
    public Mono<ResponseEnvelope<UserDetails>> getUserDetails() {
        return userService.getCurrentUserDetails()
                .map(userDetails -> ResponseEnvelope.<UserDetails>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("User Details fetched successfully")
                        .data(userDetails)
                        .build());
    }

    @GetMapping("/list")
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
