package com.ncist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("网上超市系统接口文档")
                        .description("网上超市系统接口文档")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("kim")
                                .email("2336467480@qq.com")
                                .url("http://localhost:8080")));
    }
}