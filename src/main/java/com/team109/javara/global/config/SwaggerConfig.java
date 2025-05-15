package com.team109.javara.global.config;

import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = getSecurityScheme();
        SecurityRequirement securityRequirement = getSecurityRequireMent();

        Info info = new Info()
                .version("v1.0.0")
                .title("javara API")
                .description("잡아라 API 목록입니다.")
                ;

        return new OpenAPI()
                .info(info)
                .addServersItem(new Server().url("/"))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .security(List.of(securityRequirement))
                ;
    }

    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
    }
    private SecurityRequirement getSecurityRequireMent() {
        return new SecurityRequirement().addList("bearerAuth");
    }
}

