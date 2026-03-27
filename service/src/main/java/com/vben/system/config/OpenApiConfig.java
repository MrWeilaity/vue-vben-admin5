package com.vben.system.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置（中文）。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Vben 企业管理系统 API 文档")
                .description("基于 Spring Boot + PostgreSQL + Redis 的系统管理后端接口文档")
                .version("v1.0.0")
                .license(new License().name("Apache-2.0")))
            .externalDocs(new ExternalDocumentation()
                .description("项目文档")
                .url("https://springdoc.org/"));
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("认证中心")
            .pathsToMatch("/api/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
            .group("系统管理")
            .pathsToMatch("/api/system/**")
            .build();
    }
}
