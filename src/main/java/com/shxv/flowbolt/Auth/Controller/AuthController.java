package com.shxv.flowbolt.Auth.Controller;

import com.shxv.flowbolt.Auth.DTO.LoginResponse;
import com.shxv.flowbolt.Auth.DTO.RefreshRequest;
import com.shxv.flowbolt.Auth.DTO.UserRequest;
import com.shxv.flowbolt.Auth.DTO.UserResponse;
import com.shxv.flowbolt.Auth.Service.AuthService;
import com.shxv.flowbolt.Util.ResponseEnvelope;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Tag(name = "Authentication", description = "Authentication APIs")
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid credentials or account issues"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEnvelope<LoginResponse>> login(@RequestHeader("Authorization") String authHeader) {
        log.info("Authorization header received: {}", authHeader);
        return authService.login(authHeader)
                .map(data -> new ResponseEnvelope<LoginResponse>()
                        .setSuccess(true)
                        .setStatus(HttpStatus.OK.value())
                        .setMessage("Login successful")
                        .setData(data))
                .onErrorResume(e -> {
                    String message = e.getMessage() != null ? e.getMessage() : "Login failed due to an unexpected error";

                    int status = (message.toLowerCase().contains("invalid") || message.toLowerCase().contains("locked"))
                            ? HttpStatus.UNAUTHORIZED.value()
                            : HttpStatus.BAD_REQUEST.value();

                    return Mono.just(new ResponseEnvelope<LoginResponse>()
                            .setSuccess(false)
                            .setStatus(status)
                            .setMessage(message)
                            .setData(null));
                });
    }

    @Tag(name = "Authentication", description = "Authentication APIs")
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT tokens using a valid refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Session expired or token invalid"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEnvelope<LoginResponse>> refresh(@RequestBody RefreshRequest body) {
        System.out.println(body);
        return authService.refresh(body)
                .map(data -> new ResponseEnvelope<LoginResponse>()
                        .setSuccess(true)
                        .setStatus(HttpStatus.OK.value())
                        .setMessage("Token refreshed")
                        .setData(data));
    }

    @Tag(name = "Authentication", description = "Authentication APIs")
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public Mono<ResponseEnvelope<UserResponse>> register(@RequestBody UserRequest request) {
        return authService.register(request)
                .map(user -> new ResponseEnvelope<UserResponse>()
                        .setSuccess(true)
                        .setStatus(HttpStatus.CREATED.value())
                        .setMessage("User registered successfully")
                        .setData(user));
    }

    @Tag(name = "Authentication", description = "Authentication APIs")
    @PostMapping("/logout")
    @Operation(summary = "Log out the user and invalidate the access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid Authorization header"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEnvelope<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        return authService.logout(authHeader)
                .then(Mono.just(new ResponseEnvelope<Void>()
                        .setSuccess(true)
                        .setStatus(HttpStatus.OK.value())
                        .setMessage("Logout successful")
                        .setData(null)));
    }
}
