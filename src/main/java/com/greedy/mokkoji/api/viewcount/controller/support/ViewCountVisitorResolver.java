package com.greedy.mokkoji.api.viewcount.controller.support;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
public class ViewCountVisitorResolver {

    private static final String VISITOR_COOKIE_NAME = "mokkoji_visitor_id";
    private static final String ACCOUNT_VISITOR_PREFIX = "account:";
    private static final String ANONYMOUS_VISITOR_PREFIX = "anonymous:";
    private static final Duration COOKIE_MAX_AGE = Duration.ofDays(365);

    public String resolveVisitorKey(
            final AuthCredential authCredential,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        if (authCredential.authRole() != null && authCredential.accountId() != null) {
            return ACCOUNT_VISITOR_PREFIX + authCredential.authRole().name() + ":" + authCredential.accountId();
        }

        final String existingVisitorId = findVisitorId(request.getCookies());
        if (existingVisitorId != null) {
            return ANONYMOUS_VISITOR_PREFIX + existingVisitorId;
        }

        final String visitorId = UUID.randomUUID().toString();
        final ResponseCookie visitorCookie = ResponseCookie.from(VISITOR_COOKIE_NAME, visitorId)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, visitorCookie.toString());
        return ANONYMOUS_VISITOR_PREFIX + visitorId;
    }

    private String findVisitorId(final Cookie[] cookies) {
        if (cookies == null) {
            return null;
        }

        for (final Cookie cookie : cookies) {
            if (VISITOR_COOKIE_NAME.equals(cookie.getName()) && isValidVisitorId(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isValidVisitorId(final String visitorId) {
        if (visitorId == null) {
            return false;
        }

        try {
            return UUID.fromString(visitorId).toString().equals(visitorId);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
