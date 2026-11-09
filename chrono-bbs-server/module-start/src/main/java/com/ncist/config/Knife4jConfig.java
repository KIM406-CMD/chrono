package com.ncist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 接口文档配置。
 *
 * <p>配置在线接口文档的标题、版本、联系人等基础信息。</p>
 */
@Configuration
public class Knife4jConfig {

    /**
     * 构建接口文档元信息。
     *
     * @return OpenAPI 文档配置
     */
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