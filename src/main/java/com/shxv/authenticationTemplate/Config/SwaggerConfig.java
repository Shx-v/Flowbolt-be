package com.shxv.authenticationTemplate.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowBolt API")
                        .version("1.0")
                        .description("API documentation for FlowBolt built with Spring Boot WebFlux")
                        .termsOfService("https://example.com/terms")
                        .contact(new Contact()
                                .name("Shivranjan Bharadwaj")
                                .url("https://github.com/Shx-v")
                                .email("shivranjanbharadwaj@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));

    }
}
