package com.shxv.authenticationTemplate.Security;

import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import com.shxv.authenticationTemplate.Role.Repository.PermissionRepository;
import com.shxv.authenticationTemplate.Role.Repository.RolePermissionRepository;
import com.shxv.authenticationTemplate.Security.Jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ReactiveUserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolePermissionRepository rolePermissionRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String credentials = authentication.getCredentials().toString();

        return userDetailsService.findByUsername(username)
                .switchIfEmpty(Mono.error(new BadCredentialsException("User not found")))
                .flatMap(userDetails -> userRepository.findByUsername(username)
                        .switchIfEmpty(Mono.error(new BadCredentialsException("User not found in DB")))
                        .flatMap(user -> {
                            if (!passwordEncoder.matches(credentials, userDetails.getPassword())) {
                                return Mono.error(new BadCredentialsException("Invalid credentials"));
                            }

                            UUID roleId = user.getRole();

                            return rolePermissionRepository.findAllByRoleId(roleId)
                                    .flatMap(rolePerm -> permissionRepository.findById(rolePerm.getPermissionId()))
                                    .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                                    .collectList()
                                    .map(authorities -> new UsernamePasswordAuthenticationToken(
                                            username, null, authorities));
                        })
                );
    }
}

