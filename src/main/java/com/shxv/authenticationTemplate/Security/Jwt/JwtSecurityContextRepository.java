package com.shxv.authenticationTemplate.Security.Jwt;

import com.shxv.authenticationTemplate.Auth.Repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    JwtUtils jwtUtils;


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

                    return new SecurityContextImpl(authentication);
                });
    }

}
