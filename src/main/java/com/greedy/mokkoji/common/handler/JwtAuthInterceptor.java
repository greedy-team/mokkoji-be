package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        try {
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            if (userId == null) { //로그인 미인증 사용자
                log.info("미인증 사용자 요청");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            return true;

        } catch (ExpiredJwtException e) {
            log.warn("Access Token 만료됨");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;

        } catch (JwtException e) {
            log.warn("유효하지 않은 Access Token이 입력됨");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
