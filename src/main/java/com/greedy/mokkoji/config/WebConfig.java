package com.greedy.mokkoji.config;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.UserAuthArgumentResolver;
import com.greedy.mokkoji.common.handler.JwtAuthInterceptor;
import com.greedy.mokkoji.common.handler.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Set;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Set<String> excludedPaths = Set.of("/users/auth/login", "/users/auth/refresh", "/clubs/**", "/test/health-check/**");

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final UserAuthArgumentResolver userAuthArgumentResolver;
    private final RequestLogInterceptor loggingInterceptor;
    @Value("${api.prefix}")
    private String prefixUrl;
    @Value("${cors.allowedOrigins}")
    private String[] allowedOrigins;

    public WebConfig(
            final JwtAuthInterceptor jwtAuthInterceptor,
            final RequestLogInterceptor loggingInterceptor,
            final UserAuthArgumentResolver userAuthArgumentResolver
    ) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.userAuthArgumentResolver = userAuthArgumentResolver;
        this.loggingInterceptor = loggingInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        final List<String> excludedFullPaths = excludedPaths.stream()
                .map(path -> prefixUrl + path)
                .toList();

        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(excludedFullPaths);

        registry.addInterceptor(loggingInterceptor);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userAuthArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(
                        HttpMethod.OPTIONS.name(),
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.PATCH.name()
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
