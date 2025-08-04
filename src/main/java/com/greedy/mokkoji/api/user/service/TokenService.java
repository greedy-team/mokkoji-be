package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.api.user.dto.resopnse.LoginResponse;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7;
    private final RedisRepository redisRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse generateToken(final Long userId) {
        final String accessToken = jwtUtil.generateAccessToken(userId);
        final String refreshToken = jwtUtil.generateRefreshToken(userId);
        saveRefreshToken(userId, refreshToken);
        return LoginResponse.of(accessToken, refreshToken);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        redisRepository.save("refreshToken" + userId, refreshToken, REFRESH_TOKEN_EXPIRATION);
    }

    public String getRefreshToken(Long userId) {
        return redisRepository.find("refreshToken" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        redisRepository.delete("refreshToken" + userId);
    }
}
