package com.shxv.authenticationTemplate.Security.Jwt;

import com.shxv.authenticationTemplate.Auth.Model.Session;
import com.shxv.authenticationTemplate.Auth.Repository.SessionRepository;
import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Model.Permission;
import com.shxv.authenticationTemplate.Role.Service.PermissionService;
import com.shxv.authenticationTemplate.Security.CustomAuthenticationManager;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    CustomAuthenticationManager authenticationManager;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    ReactiveUserDetailsService userDetailsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PermissionService permissionService;


    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        // Stateless, nothing to save
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {

        return Mono.defer(() -> {
                    String authHeader = exchange.getRequest()
                            .getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);

                    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        return Mono.empty();
                    }

                    return Mono.just(authHeader.substring(7));
                })
                .flatMap(token ->
                        jwtUtils.isTokenValid(token)
                                .filter(Boolean::booleanValue)
                                .thenReturn(token)
                )
                .flatMap(token ->
                        sessionRepository.findByAccessTokenAndActiveIsTrue(token)
                )
                .flatMap(session ->
                        jwtUtils.extractAllClaims(session.getAccessToken())
                )
                .map(claims -> {

                    Map<String, Object> user = claims.get("user", Map.class);

                    UUID userId = UUID.fromString(user.get("id").toString());
                    UUID roleId = UUID.fromString(user.get("roleId").toString());
                    String username = claims.getSubject();

                    @SuppressWarnings("unchecked")
                    List<String> permissions =
                            (List<String>) user.getOrDefault("permissions", List.of());

                    List<GrantedAuthority> authorities = permissions.stream()
                            .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p))
                            .toList();

                    UserPrincipal principal = new UserPrincipal(
                            userId,
                            username,
                            roleId,
                            permissions
                    );

                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    authorities
                            );

                    return (SecurityContext) new SecurityContextImpl(authentication);
                });
    }

}
