package com.shxv.flowbolt.Auth.Service;

import com.shxv.flowbolt.Auth.DTO.LoginResponse;
import com.shxv.flowbolt.Auth.DTO.RefreshRequest;
import com.shxv.flowbolt.Auth.DTO.UserRequest;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<LoginResponse> login(String authHeader);

    Mono<LoginResponse> refresh(RefreshRequest body);

    Mono<UserResponse> register(UserRequest userRequest);

    Mono<Void> logout(String authHeader);
}
