package com.greedy.mokkoji.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    private final SwaggerDescription swaggerDescription;

    @Value("${swagger.base-url}")
    private String swaggerServerUrl;

    public SwaggerConfig(SwaggerDescription swaggerDescription) {
        this.swaggerDescription = swaggerDescription;
    }

    // 서버 URL 설정
    @Bean
    public OpenApiCustomizer serverOpenApiCustomizer() {
        return openApi -> openApi.setServers(
                List.of(new Server().url(swaggerServerUrl))
        );
    }

    // 실제 Swagger UI의 Info 설정
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
                        .version("1.0.0")
                        .description(swaggerDescription.getDescription())  // 💥 동적으로 ENUM HTML 적용
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
