package com.shxv.authenticationTemplate.Auth.Service;

import com.shxv.authenticationTemplate.Auth.DTO.UserDetails;
import com.shxv.authenticationTemplate.Auth.DTO.UserListResponse;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface UserService {
    Flux<UserResponse> getAllUsers();

    Mono<UserResponse> getUserById(UUID uuid);

    Mono<UserDetails> getUserDetailsById(UUID uuid);

    Mono<UserDetails> getCurrentUserDetails();

    Flux<UserListResponse> getUserList();
}
