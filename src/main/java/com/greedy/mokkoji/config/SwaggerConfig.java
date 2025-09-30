package com.greedy.mokkoji.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://43.200.8.39:8080/", description = "개발 서버"),
                @Server(url = "http://localhost:8080/", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";

        // 보안 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt);
        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 최종 OpenAPI 객체 반환
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(jwt, securityScheme))
                .info(apiInfo())
                .addSecurityItem(securityRequirement);
    }

    private Info apiInfo() {
        return new Info()
                .title("mokkoji API 명세서")
                .description("mokkoji 서비스 API 명세서")
                .version("1.0.0");
    }
}

