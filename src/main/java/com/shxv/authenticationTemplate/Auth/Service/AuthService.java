package com.shxv.authenticationTemplate.Auth.Service;

import com.shxv.authenticationTemplate.Auth.DTO.LoginResponse;
import com.shxv.authenticationTemplate.Auth.DTO.RefreshRequest;
import com.shxv.authenticationTemplate.Auth.DTO.UserRequest;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<LoginResponse> login(String authHeader);

    Mono<LoginResponse> refresh(RefreshRequest body);

    Mono<UserResponse> register(UserRequest userRequest);

    Mono<Void> logout(String authHeader);
}
