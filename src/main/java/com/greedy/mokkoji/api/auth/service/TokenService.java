
package com.greedy.mokkoji.api.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final StringRedisTemplate redisTemplate;
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    public void saveRefreshToken(Long userId, String refreshToken) {
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
