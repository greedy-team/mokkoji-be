package com.greedy.mokkoji.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "auth.csrf")
public record CsrfProperties(
        List<String> allowedOrigins
) {
    public CsrfProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }

    public boolean isAllowed(final String origin) {
        return origin != null && allowedOrigins.contains(origin);
    }
}
