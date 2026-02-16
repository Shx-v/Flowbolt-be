package com.shxv.authenticationTemplate.Auth.Controller;

import com.shxv.authenticationTemplate.Auth.Util.UserRoleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class TestController {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserRoleUtil userRoleUtil;

    @GetMapping("/public/hello")
    public Mono<String> publicHello() {
        System.out.println(passwordEncoder.encode("root@123"));
        return Mono.just("Hello from public endpoint");
    }

    @GetMapping("/secure/hello")
    public Mono<Authentication> secureHello() {
        return userRoleUtil.getAuthentication();
    }
}
