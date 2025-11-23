package com.greedy.mokkoji.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;

import java.util.Arrays;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "mokkoji API 명세서",
                version = "1.0.0",
                description = SwaggerDescription.ENUM_DESCRIPTION
        ),
        servers = {
                @Server(url = "http://43.200.8.39:8080/", description = "개발 서버"),
                @Server(url = "http://localhost:8080/", description = "로컬 서버")
        }
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";

        SecurityScheme securityScheme = new SecurityScheme()
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(jwt, securityScheme))
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("mokkoji API 명세서")
                        .description("mokkoji 서비스 API 명세서입니다. ENUM 용어 설명은 전역 Info 참조.")
                        .version("1.0.0")
                        .contact(new Contact().name("Mokkoji Dev Team").email("mokkoji@example.com"))
                );
    }

    // Swagger 문서에서 @Authentication AuthCredential 파라미터 숨기기
    @Bean
    public OperationCustomizer hideAuthCredentialParameter() {
        return (operation, handlerMethod) -> {
            Arrays.stream(handlerMethod.getMethodParameters())
                    .filter(this::isAuthCredentialParameter)
                    .findAny()
                    .ifPresent(param -> removeAuthCredentialParameter(operation));
            return operation;
        };
    }

    private boolean isAuthCredentialParameter(MethodParameter param) {
        return "AuthCredential".equals(param.getParameterType().getSimpleName());
    }

    private void removeAuthCredentialParameter(io.swagger.v3.oas.models.Operation operation) {
        if (operation.getParameters() == null) {
            return;
        }
        operation.getParameters().removeIf(p ->
                "authCredential".equalsIgnoreCase(p.getName())
        );
    }
}
