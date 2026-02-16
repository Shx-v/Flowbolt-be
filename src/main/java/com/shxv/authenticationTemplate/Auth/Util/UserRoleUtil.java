package com.shxv.authenticationTemplate.Auth.Util;

import com.shxv.authenticationTemplate.Security.Jwt.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class UserRoleUtil {

    public Mono<Authentication> getAuthentication() {
        return ReactiveSecurityContextHolder.getContext().map(SecurityContext::getAuthentication);
    }

    public Mono<UserPrincipal> getUserContext() {
        return getAuthentication().map(auth -> (UserPrincipal) auth.getPrincipal());
    }

    public Mono<UUID> getUserId() {
        return getUserContext().map(UserPrincipal::getUserId);
    }

    public Mono<String> getUsername() {
        return getUserContext().map(UserPrincipal::getUsername);
    }

    public Mono<UUID> getRoleId() {
        return getUserContext().map(UserPrincipal::getRoleId);
    }

    public Mono<List<String>> getPermissions() {
        return getUserContext().map(UserPrincipal::getPermissions);
    }

    public Mono<Boolean> hasPermission(String perm) {
        return getPermissions()
                .map(list -> list.contains(perm));
    }

    public Mono<Boolean> hasAnyPermission(List<String> perms) {
        return getPermissions()
                .map(list -> list.stream().anyMatch(perms::contains));
    }
}
