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
                .securityContextRepository(securityContextRepository)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/docs", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .pathMatchers("/public/**", "/auth/login", "/auth/refresh").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("auth/register").hasAnyAuthority("ADMIN_ACCESS", "CREATE_USER")
                        .pathMatchers(HttpMethod.GET, "/role").hasAnyAuthority("ADMIN_ACCESS", "READ_ROLE")
                        .pathMatchers(HttpMethod.POST, "/role").hasAnyAuthority("ADMIN_ACCESS", "CREATE_ROLE")
                        .pathMatchers(HttpMethod.PUT, "/role").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_ROLE")
                        .pathMatchers(HttpMethod.DELETE, "/role").hasAnyAuthority("ADMIN_ACCESS", "DELETE_ROLE")
                        .pathMatchers(HttpMethod.POST, "/project").hasAnyAuthority("ADMIN_ACCESS", "CREATE_PROJECT")
                        .pathMatchers(HttpMethod.GET, "/project").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT")
                        .pathMatchers(HttpMethod.PUT, "/project").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_PROJECT")
                        .pathMatchers(HttpMethod.DELETE, "/project").hasAnyAuthority("ADMIN_ACCESS", "DELETE_PROJECT")
                        .pathMatchers(HttpMethod.POST, "/project/*/archive").hasAnyAuthority("ADMIN_ACCESS", "ARCHIVE_PROJECT")
                        .pathMatchers(HttpMethod.POST, "/project/*/suspend").hasAnyAuthority("ADMIN_ACCESS", "SUSPEND_PROJECT")
                        .pathMatchers(HttpMethod.POST, "/project/*/restore").hasAnyAuthority("ADMIN_ACCESS", "RESTORE_PROJECT")
                        .pathMatchers(HttpMethod.POST, "/project/*/transfer-ownership").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_PROJECT")
                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
