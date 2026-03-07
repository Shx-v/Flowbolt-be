package com.shxv.flowbolt.Auth.Controller;

import com.shxv.flowbolt.Auth.Util.UserRoleUtil;
import com.shxv.flowbolt.ProjectMember.Util.PermissionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class TestController {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRoleUtil userRoleUtil;

    @Autowired
    PermissionResolver permissionResolver;

    @GetMapping("/public/hello")
    public Mono<String> publicHello() {
        System.out.println(passwordEncoder.encode("root@123"));
        return Mono.just("Hello from public endpoint");
    }

    @GetMapping("/secure/hello")
    public Mono<Authentication> secureHello() {
        return userRoleUtil.getAuthentication();
    }

    @GetMapping("/perm-test/{perm}")
    public Mono<Boolean> checkPermission(@PathVariable("perm") String perm) {
        return permissionResolver.hasPermission(
                perm,
                UUID.fromString("a19a4fe0-2316-4850-9af0-4724dccf10d5"),
                UUID.fromString("17b4f109-54e0-4e8a-a840-fdf4a8df83c2")
        );
    }
}
