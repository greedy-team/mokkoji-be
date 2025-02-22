package com.greedy.mokkoji.config;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.UserAuthArgumentResolver;
import com.greedy.mokkoji.common.handler.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final UserAuthArgumentResolver userAuthArgumentResolver;

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
                .excludePathPatterns("/auth/login", "/auth/refresh");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userAuthArgumentResolver);
    }
}

