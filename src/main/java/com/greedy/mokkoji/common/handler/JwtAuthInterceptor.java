package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.service.AuthTokenResolver;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final AuthTokenResolver authTokenResolver;
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

        final String token = authTokenResolver.resolveToken(request)
                .orElseThrow(() -> new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER));
        final AuthCredential credential = jwtUtil.getCredentialFromToken(token);
        return credential != null;
    }
}
