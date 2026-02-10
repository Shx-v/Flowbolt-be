package com.shxv.authenticationTemplate.Security;

import com.shxv.authenticationTemplate.Auth.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class CustomAuthenticationManager implements ReactiveAuthenticationManager {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String username = authentication.getName();
        String credentials = authentication.getCredentials().toString();

        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new BadCredentialsException("Invalid credentials")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(credentials, user.getPassword())) {
                        return Mono.error(new BadCredentialsException("Invalid credentials"));
                    }

                    return Mono.just(
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(),
                                    null,
                                    List.of()
                            )
                    );
                });
    }
}

