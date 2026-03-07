package com.shxv.flowbolt.Config;

import com.shxv.flowbolt.Security.Jwt.JwtSecurityContextRepository;
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
                                //USER END-POINTS
                                .pathMatchers("auth/register").hasAnyAuthority("ADMIN_ACCESS", "CREATE_USER")
                                //ROLE END-POINTS
                                .pathMatchers(HttpMethod.GET, "/role").hasAnyAuthority("ADMIN_ACCESS", "READ_ROLE")
                                .pathMatchers(HttpMethod.GET, "/role/permission").hasAnyAuthority("ADMIN_ACCESS", "READ_ROLE")
                                .pathMatchers(HttpMethod.GET, "/role/*").hasAnyAuthority("ADMIN_ACCESS", "READ_ROLE")
                                .pathMatchers(HttpMethod.POST, "/role").hasAnyAuthority("ADMIN_ACCESS", "CREATE_ROLE")
                                .pathMatchers(HttpMethod.PUT, "/role/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_ROLE")
                                .pathMatchers(HttpMethod.DELETE, "/role/*").hasAnyAuthority("ADMIN_ACCESS", "DELETE_ROLE")
                                //PROJECT END-POINTS
                                .pathMatchers(HttpMethod.GET, "/project").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT")
                                .pathMatchers(HttpMethod.GET, "/project/*").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT")
                                .pathMatchers(HttpMethod.GET, "/project/details/*").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT")
                                .pathMatchers(HttpMethod.GET, "/project/code/*").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT")
                                .pathMatchers(HttpMethod.POST, "/project").hasAnyAuthority("ADMIN_ACCESS", "CREATE_PROJECT")
                                .pathMatchers(HttpMethod.POST, "/project/*/archive").hasAnyAuthority("ADMIN_ACCESS", "ARCHIVE_PROJECT")
                                .pathMatchers(HttpMethod.POST, "/project/*/suspend").hasAnyAuthority("ADMIN_ACCESS", "SUSPEND_PROJECT")
                                .pathMatchers(HttpMethod.POST, "/project/*/restore").hasAnyAuthority("ADMIN_ACCESS", "RESTORE_PROJECT")
                                .pathMatchers(HttpMethod.POST, "/project/*/transfer-ownership").hasAnyAuthority("ADMIN_ACCESS", "TRANSFER_PROJECT_OWNERSHIP")
                                .pathMatchers(HttpMethod.PUT, "/project/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_PROJECT")
                                .pathMatchers(HttpMethod.DELETE, "/project/*").hasAnyAuthority("ADMIN_ACCESS", "DELETE_PROJECT")
                                //GROUP END-POINTS
                                .pathMatchers(HttpMethod.GET, "/group").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                //.pathMatchers(HttpMethod.GET, "/group/my").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                .pathMatchers(HttpMethod.GET, "/group/list").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                .pathMatchers(HttpMethod.GET, "/group/details/*").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                .pathMatchers(HttpMethod.GET, "/group/*").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                .pathMatchers(HttpMethod.POST, "/group").hasAnyAuthority("ADMIN_ACCESS", "CREATE_GROUP")
                                .pathMatchers(HttpMethod.POST, "/group/*/member").hasAnyAuthority("ADMIN_ACCESS", "CREATE_GROUP")
                                .pathMatchers(HttpMethod.PUT, "/group/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_GROUP")
                                .pathMatchers(HttpMethod.PUT, "/group/*/transfer").hasAnyAuthority("ADMIN_ACCESS", "TRANSFER_GROUP_LEADERSHIP")
                                .pathMatchers(HttpMethod.PUT, "/group/*/archive").hasAnyAuthority("ADMIN_ACCESS", "ARCHIVE_GROUP")
                                .pathMatchers(HttpMethod.PUT, "/group/*/restore").hasAnyAuthority("ADMIN_ACCESS", "RESTORE_GROUP")
                                .pathMatchers(HttpMethod.DELETE, "/group/*/member/*").hasAnyAuthority("ADMIN_ACCESS", "CREATE_GROUP")
                                //.pathMatchers(HttpMethod.DELETE, "/group/*/leave").hasAnyAuthority("ADMIN_ACCESS", "READ_GROUP")
                                //PROJECT MEMBER END-POINTS
                                .pathMatchers(HttpMethod.GET, "/project-member/*").hasAnyAuthority("ADMIN_ACCESS","READ_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.GET, "/project-member/*/member/*").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.GET, "/project-member/*/delegated-permissions").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.GET, "/project-member/permissions").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.GET, "/project-member/member/*").hasAnyAuthority("ADMIN_ACCESS", "READ_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.POST, "/project-member").hasAnyAuthority("ADMIN_ACCESS", "CREATE_PROJECT_MEMBERS")
                                //.pathMatchers(HttpMethod.POST, "/project-member/delegate-permissions").hasAnyAuthority("ADMIN_ACCESS", "CREATE_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.PUT, "/project-member/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_PROJECT_MEMBERS")
                                .pathMatchers(HttpMethod.DELETE, "/project-member/*").hasAnyAuthority("ADMIN_ACCESS", "DELETE_PROJECT_MEMBERS")
                                //TICKET END-POINTS
                                .pathMatchers(HttpMethod.GET, "/ticket").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.GET, "/ticket/*").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.GET, "/ticket/types").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.GET, "/ticket/status-transitions/*/*").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.GET, "/ticket/project/*").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.GET, "/ticket/details/*").hasAnyAuthority("ADMIN_ACCESS", "READ_TICKET")
                                .pathMatchers(HttpMethod.POST, "/ticket").hasAnyAuthority("ADMIN_ACCESS", "CREATE_TICKET")
                                .pathMatchers(HttpMethod.PUT, "/ticket/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_TICKET")
                                .pathMatchers(HttpMethod.PATCH, "/ticket/status").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_TICKET")
                                .pathMatchers(HttpMethod.PATCH, "/ticket/priority").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_TICKET")
                                .pathMatchers(HttpMethod.PATCH, "/ticket/assignee").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_TICKET")
                                .pathMatchers(HttpMethod.DELETE, "/ticket/*").hasAnyAuthority("ADMIN_ACCESS", "DELETE_TICKET")
                                //TICKET COMMENT END-POINTS
                                .pathMatchers(HttpMethod.GET, "/comment/ticket/*").hasAnyAuthority("ADMIN_ACCESS", "READ_COMMENT")
                                .pathMatchers(HttpMethod.POST, "/comment").hasAnyAuthority("ADMIN_ACCESS", "CREATE_COMMENT")
                                .pathMatchers(HttpMethod.PUT, "/comment/*").hasAnyAuthority("ADMIN_ACCESS", "UPDATE_COMMENT")
                                .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}
