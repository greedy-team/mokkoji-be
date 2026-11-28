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

    /**
     * Authorization 헤더가 있으면 헤더를, 없으면 access_token 쿠키를 사용한다.
     * 헤더가 우선이므로 기존 Bearer 방식 클라이언트는 영향을 받지 않는다.
     */
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
