package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.jwt.BearerAuthExtractor;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final BearerAuthExtractor bearerAuthExtractor;
    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler
    ) {
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String token = bearerAuthExtractor.extractTokenValue(header);
        final Long userId = jwtUtil.getUserIdFromToken(token);
        return userId != null;
    }
}
