package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public LoginResponse generateToken(final Long userId) {
        final String accessToken = jwtUtil.generateAccessToken(userId);
        final String refreshToken = jwtUtil.generateRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);
        return LoginResponse.of(accessToken, refreshToken);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refreshToken:" + userId,
                refreshToken,
                REFRESH_TOKEN_EXPIRATION,
                TimeUnit.SECONDS
        );
    }

    public String getRefreshToken(Long userId) {
        return redisTemplate.opsForValue().get("refreshToken:" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisTemplate.delete("refreshToken:" + userId);
    }
}
