package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.auth.config.CsrfProperties;
import com.greedy.mokkoji.api.auth.service.AuthTokenResolver;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * 쿠키로 인증된 상태 변경 요청(POST/PUT/PATCH/DELETE)의 Origin 을 검사한다.
 * <p>
 * 쿠키는 브라우저가 자동으로 붙이므로 다른 사이트가 사용자 권한으로 요청을 유발할 수 있다(CSRF).
 * Bearer 헤더로 온 요청은 다른 사이트가 헤더를 붙일 수 없으므로 검사하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class CsrfOriginInterceptor implements HandlerInterceptor {

    private static final Set<String> STATE_CHANGING_METHODS = Set.of(
            HttpMethod.POST.name(),
            HttpMethod.PUT.name(),
            HttpMethod.PATCH.name(),
            HttpMethod.DELETE.name()
    );

    private final AuthTokenResolver authTokenResolver;
    private final CsrfProperties csrfProperties;

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        if (!STATE_CHANGING_METHODS.contains(request.getMethod())) {
            return true;
        }

        if (!authTokenResolver.isCookieAuthenticated(request)) {
            return true;
        }

        final String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (!csrfProperties.isAllowed(origin)) {
            throw new MokkojiException(FailMessage.FORBIDDEN_INVALID_ORIGIN);
        }

        return true;
    }
}
