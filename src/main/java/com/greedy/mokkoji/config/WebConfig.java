package com.greedy.mokkoji.config;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.UserAuthArgumentResolver;
import com.greedy.mokkoji.common.handler.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final UserAuthArgumentResolver userAuthArgumentResolver;

    @Value("${api.prefix}")
    private String prefixUrl;

    public WebConfig(
            JwtAuthInterceptor jwtAuthInterceptor,
            UserAuthArgumentResolver userAuthArgumentResolver
    ) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.userAuthArgumentResolver = userAuthArgumentResolver;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(prefixUrl + "/users/auth/login", prefixUrl + "/users/auth/refresh", prefixUrl + "/clubs/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userAuthArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173", "https://mokkoji.vercel.app")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}

