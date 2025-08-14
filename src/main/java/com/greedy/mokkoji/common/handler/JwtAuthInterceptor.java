package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.jwt.AuthExtractor;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final AuthExtractor authExtractor;

    @Override
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        final String token = authExtractor.extractAccessToken(request);
        final Long userId = jwtUtil.getUserIdFromToken(token);
        return userId != null;
    }
}
