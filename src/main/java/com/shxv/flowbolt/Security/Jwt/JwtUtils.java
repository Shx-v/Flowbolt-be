package com.shxv.flowbolt.Security.Jwt;

import com.shxv.flowbolt.Auth.Model.User;
import com.shxv.flowbolt.Role.Service.GlobalPermissionService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expirationMs}")
    private long expirationMs;

    private Key key;

    @Autowired
    GlobalPermissionService permissionService;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Mono<String> generateToken(User user, List<String> perms) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return convertUserToMap(user, perms)
                .flatMap(userJwtDTO ->
                        Mono.fromCallable(() ->
                                Jwts.builder()
                                        .setSubject(user.getUsername())
                                        .claim("user", userJwtDTO)
                                        .setIssuedAt(now)
                                        .setExpiration(expiryDate)
                                        .signWith(key, SignatureAlgorithm.HS256)
                                        .compact()
                        )
                );
    }

    public Mono<String> generateRefreshToken(User user, List<String> perms) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (30L * 24 * 60 * 60 * 1000));

        return convertUserToMap(user, perms)
                .flatMap(userJwtDTO ->
                        Mono.fromCallable(() ->
                                Jwts.builder()
                                        .setSubject(user.getUsername())
                                        .claim("user", userJwtDTO)
                                        .setIssuedAt(now)
                                        .setExpiration(expiryDate)
                                        .signWith(key, SignatureAlgorithm.HS256)
                                        .compact()
                        )
                );
    }

    public Mono<String> extractUsername(String token) {
        return extractAllClaims(token)
                .map(Claims::getSubject)
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<UUID> extractUser(String token) {
        return extractAllClaims(token)
                .map(claims -> {
                    Map<String, Object> user =
                            claims.get("user", Map.class);

                    Object id = user.get("id");
                    if (id == null) {
                        throw new IllegalArgumentException("User ID missing in token");
                    }

                    return UUID.fromString(id.toString());
                })
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<Boolean> isTokenValid(String token) {
        return extractAllClaims(token)
                .map(claims -> {
                    Date expiration = claims.getExpiration();
                    return expiration != null && expiration.after(new Date());
                })
                .onErrorReturn(false);
    }

    public Mono<Claims> extractAllClaims(String token) {
        return Mono.fromCallable(() ->
                Jwts.parserBuilder()
                        .setSigningKey(this.key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
        );
    }

    private Mono<Map<String, Object>> convertUserToMap(User user, List<String> perms) {
        return Mono.fromSupplier(() -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("roleId", user.getRole());
            map.put("permissions", perms);
            return map;
        });
    }

}
