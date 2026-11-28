package com.greedy.mokkoji.api.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.cookie")
public record CookieProperties(
        String domain,
        boolean secure
) {
}
