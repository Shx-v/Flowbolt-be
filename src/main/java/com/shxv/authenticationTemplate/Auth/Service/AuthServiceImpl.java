package com.shxv.authenticationTemplate.Auth.Service;

import com.shxv.authenticationTemplate.Auth.DTO.LoginResponse;
import com.shxv.authenticationTemplate.Auth.DTO.RefreshRequest;
import com.shxv.authenticationTemplate.Auth.DTO.UserRequest;
import com.shxv.authenticationTemplate.Auth.DTO.UserResponse;
import com.shxv.authenticationTemplate.Auth.Model.Session;
import com.shxv.authenticationTemplate.Auth.Model.User;
import com.shxv.authenticationTemplate.Auth.Repository.SessionRepository;
import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Model.GlobalPermission;
import com.shxv.authenticationTemplate.Role.Service.GlobalPermissionService;
import com.shxv.authenticationTemplate.Security.Jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Comparator;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_SESSIONS = 3;

    @Autowired
    ReactiveAuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    GlobalPermissionService permissionService;

    @Override
    public Mono<LoginResponse> login(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return Mono.error(new IllegalArgumentException("Missing or invalid Authorization header"));
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
        String credentials = new String(decodedBytes);
        String[] values = credentials.split(":", 2);

        if (values.length != 2) {
            return Mono.error(new IllegalArgumentException("Invalid Basic authentication token"));
        }

        String username = values[0];
        String password = values[1];
        Authentication authToken = new UsernamePasswordAuthenticationToken(username, password);

        return authenticationManager.authenticate(authToken)
                .flatMap(auth -> userRepository.findByUsername(auth.getName()))
                .flatMap(user -> {
                    if (!user.isVerified()) {
                        return Mono.error(new IllegalStateException("User account is not verified"));
                    }

                    if (user.isAccountLocked()) {
                        return Mono.error(new IllegalStateException("User account is locked"));
                    }

                    return sessionRepository.findByUserIdAndActiveIsTrue(user.getId())
                            .collectList()
                            .flatMap(sessions -> {
                                if (sessions.size() >= MAX_SESSIONS) {
                                    Session oldest = sessions.stream()
                                            .min(Comparator.comparing(Session::getIssuedAt))
                                            .orElse(null);

                                    oldest.setActive(false);
                                    return sessionRepository.save(oldest).thenReturn(user);
                                }
                                return Mono.just(user);
                            })
                            .flatMap(u -> permissionService.getAllPermissions(u.getRole())
                                    .map(GlobalPermission::getKey)
                                    .collectList()
                                    .flatMap(perms -> Mono.zip(
                                                    jwtUtils.generateToken(u, perms),
                                                    jwtUtils.generateRefreshToken(u, perms)
                                            )
                                            .flatMap(tuple -> {
                                                String accessToken = tuple.getT1();
                                                String refreshToken = tuple.getT2();

                                                Session session = new Session()
                                                        .setUserId(u.getId())
                                                        .setAccessToken(accessToken)
                                                        .setRefreshToken(refreshToken)
                                                        .setIssuedAt(Instant.now())
                                                        .setExpiresAt(Instant.now().plusSeconds(3600))
                                                        .setActive(true);

                                                return sessionRepository.save(session)
                                                        .doOnSuccess(System.out::println)
                                                        .map(saved -> new LoginResponse(accessToken, refreshToken));
                                            })));
                });
    }

    @Override
    public Mono<LoginResponse> refresh(RefreshRequest body) {

        return Mono.defer(() -> {
                    String refreshToken = body.getRefreshToken();

                    if (refreshToken == null) {
                        return Mono.error(new IllegalArgumentException("Refresh token missing"));
                    }

                    return Mono.just(refreshToken);
                })
                .flatMap(token ->
                        jwtUtils.isTokenValid(token)
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token")))
                                .thenReturn(token)
                )
                .flatMap(jwtUtils::extractUser)
                .flatMap(userId ->
                        userRepository.findById(userId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")))
                )
                .flatMap(user ->
                        sessionRepository.findByRefreshTokenAndActiveIsTrue(body.getRefreshToken())
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Session not found or expired")))
                                .flatMap(existingSession -> permissionService.getAllPermissions(user.getRole())
                                        .map(GlobalPermission::getKey)
                                        .collectList()
                                        .flatMap(perms -> Mono.zip(
                                                jwtUtils.generateToken(user, perms),
                                                jwtUtils.generateRefreshToken(user, perms)
                                        ).flatMap(tokens -> {

                                            String newAccessToken = tokens.getT1();
                                            String newRefreshToken = tokens.getT2();

                                            existingSession.setAccessToken(newAccessToken);
                                            existingSession.setRefreshToken(newRefreshToken);
                                            existingSession.setExpiresAt(Instant.now().plusSeconds(3600));

                                            return sessionRepository.save(existingSession)
                                                    .map(saved ->
                                                            new LoginResponse(newAccessToken, newRefreshToken)
                                                    );
                                        }))
                                )
                );
    }


    @Override
    public Mono<UserResponse> register(UserRequest request) {
        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    } else {
                        User user = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .email(request.getEmail())
                                .phoneNumber(request.getPhoneNumber())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .role(request.getRole())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();

                        return userRepository.save(user)
                                .map(this::mapToUserResponse);
                    }
                });
    }

    @Override
    public Mono<Void> logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);

        return sessionRepository.findByAccessTokenAndActiveIsTrue(token)
                .flatMap(session -> {
                    session.setActive(false);
                    return sessionRepository.save(session);
                })
                .then();
    }

    //HELPER METHODS
    private UserResponse mapToUserResponse(User user) {
        System.out.println(user);
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .logoPath(user.getLogoPath())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}
