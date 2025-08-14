package com.greedy.mokkoji.api.jwt;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthExtractor {

    public String extractAccessToken(HttpServletRequest request) {
        return extractTokenFromCookie(request, "accessToken");
    }

    public String extractRefreshToken(HttpServletRequest request) {
        return extractTokenFromCookie(request, "refreshToken");
    }

    private String extractTokenFromCookie(final HttpServletRequest request, final String cookieName) {
        if (request.getCookies() == null) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER);
        }

        for (Cookie cookie : request.getCookies()) {
            if (Objects.equals(cookieName, cookie.getName())) {
                final String token = cookie.getValue();
                if (token == null || token.isBlank()) {
                    throw new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER);
                }
                return token;
            }
        }

        throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
    }
}
