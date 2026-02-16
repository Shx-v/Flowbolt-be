package com.shxv.authenticationTemplate.Config;

import com.shxv.authenticationTemplate.Security.Jwt.JwtSecurityContextRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtSecurityContextRepository securityContextRepository) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
//                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**","/docs", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .pathMatchers("/public/**", "/auth/login", "/auth/refresh", "auth/register").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
//                        .pathMatchers(HttpMethod.GET,"/role").hasAuthority("READ_ROLE")
//                        .pathMatchers(HttpMethod.POST,"/role").hasAuthority("CREATE_ROLE")
//                        .pathMatchers(HttpMethod.PUT,"/role").hasAuthority("UPDATE_ROLE")
//                        .pathMatchers(HttpMethod.DELETE,"/role").hasAuthority("DELETE_ROLE")
//                        .pathMatchers(HttpMethod.GET,"/project").hasAuthority("READ_PROJECT")
//                        .pathMatchers(HttpMethod.POST,"/project").hasAuthority("CREATE_PROJECT")
//                        .pathMatchers(HttpMethod.PUT,"/project").hasAuthority("UPDATE_PROJECT")
//                        .pathMatchers(HttpMethod.DELETE,"/project").hasAuthority("DELETE_PROJECT")
//                        .pathMatchers(HttpMethod.GET,"/ticket").hasAuthority("READ_TICKET")
//                        .pathMatchers(HttpMethod.POST,"/ticket").hasAuthority("CREATE_TICKET")
//                        .pathMatchers(HttpMethod.PUT,"/ticket").hasAuthority("UPDATE_TICKET")
//                        .pathMatchers(HttpMethod.DELETE,"/ticket").hasAuthority("DELETE_TICKET")
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
