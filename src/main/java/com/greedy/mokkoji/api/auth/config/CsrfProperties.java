package com.greedy.mokkoji.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "auth.csrf")
public record CsrfProperties(
        List<String> allowedOrigins
) {
    public CsrfProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : allowedOrigins.stream()
                .filter(origin -> origin != null && !origin.isBlank())
                .map(String::trim)
                .toList();
    }

    public boolean isAllowed(final String origin) {
        return origin != null && allowedOrigins.contains(origin);
    }
}
