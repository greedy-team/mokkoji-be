package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.api.auth.config.CookieProperties;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import org.springframework.boot.web.server.Cookie.SameSite;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieFactory {

    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    private static final String PATH = "/";

    private final CookieProperties cookieProperties;

    public AuthCookieFactory(final CookieProperties cookieProperties) {
        this.cookieProperties = cookieProperties;
    }

    public ResponseCookie accessToken(final String token) {
        return build(ACCESS_TOKEN_COOKIE, token, Duration.ofMillis(JwtUtil.ACCESS_TOKEN_EXPIRATION));
    }

    public ResponseCookie refreshToken(final String token) {
        return build(REFRESH_TOKEN_COOKIE, token, Duration.ofMillis(JwtUtil.REFRESH_TOKEN_EXPIRATION));
    }

    public ResponseCookie expiredAccessToken() {
        return build(ACCESS_TOKEN_COOKIE, "", Duration.ZERO);
    }

    public ResponseCookie expiredRefreshToken() {
        return build(REFRESH_TOKEN_COOKIE, "", Duration.ZERO);
    }

    private ResponseCookie build(final String name, final String value, final Duration maxAge) {
        final ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(SameSite.LAX.attributeValue())
                .path(PATH)
                .maxAge(maxAge);

        if (hasDomain()) {
            builder.domain(cookieProperties.domain());
        }

        return builder.build();
    }

    private boolean hasDomain() {
        return cookieProperties.domain() != null && !cookieProperties.domain().isBlank();
    }
}
