package com.greedy.mokkoji.api.auth.service;

import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthTokenResolver {

    private final BearerAuthExtractor bearerAuthExtractor;

    public Optional<String> resolveToken(final HttpServletRequest request) {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            return Optional.of(bearerAuthExtractor.extractTokenValue(header));
        }
        return readCookie(request, AuthCookieFactory.ACCESS_TOKEN_COOKIE);
    }

    public Optional<String> resolveRefreshToken(final HttpServletRequest request) {
        return readCookie(request, AuthCookieFactory.REFRESH_TOKEN_COOKIE);
    }

    public boolean isCookieAuthenticated(final HttpServletRequest request) {
        return request.getHeader(HttpHeaders.AUTHORIZATION) == null
                && readCookie(request, AuthCookieFactory.ACCESS_TOKEN_COOKIE).isPresent();
    }

    private Optional<String> readCookie(final HttpServletRequest request, final String name) {
        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
