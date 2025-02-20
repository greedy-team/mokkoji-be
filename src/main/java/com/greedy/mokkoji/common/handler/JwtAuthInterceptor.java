package com.greedy.mokkoji.common.handler;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtAuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String accessToken = request.getHeader("accessToken");

        try {
            jwtUtil.getUserIdFromToken(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            log.info("Access Token 만료됨");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write("{\"error\": \"Access Token이 만료되었습니다.\"}");
            } catch (IOException ex) {
                log.error("IOException 발생", ex);
            }
            return false;
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            try {
                response.getWriter().write("{\"error\": \"유효하지 않은 Access Token입니다.\"}");
            } catch (IOException ex) {
                log.error("IOException 발생", ex);
            }
            return false;
        }
    }

}

