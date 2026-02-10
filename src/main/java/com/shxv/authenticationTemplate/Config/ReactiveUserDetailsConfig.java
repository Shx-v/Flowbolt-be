package com.shxv.authenticationTemplate.Config;

import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Repository.GlobalPermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RoleGlobalPermissionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Configuration
public class ReactiveUserDetailsConfig {

    @Bean
    public ReactiveUserDetailsService userDetailsService(
            UserRepository userRepository,
            RoleGlobalPermissionRepository roleGlobalPermissionRepository,
            GlobalPermissionRepository globalPermissionRepository
    ) {
        return username -> userRepository.findByUsername(username)
                .flatMap(appUser -> {
                    UUID roleId = appUser.getRole();
                    Mono<List<SimpleGrantedAuthority>> authoritiesMono = roleGlobalPermissionRepository.findAllByRoleId(roleId)
                            .flatMap(rolePermission -> globalPermissionRepository.findById(rolePermission.getPermissionId()))
                            .map(permission -> new SimpleGrantedAuthority(permission.getKey()))
                            .collectList();

                    return authoritiesMono.map(authorities -> new User(
                            appUser.getUsername(),
                            appUser.getPassword(),
                            authorities
                    ));
                });
    }
}
