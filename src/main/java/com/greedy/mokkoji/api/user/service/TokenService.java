package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.auth.controller.argumentResolver.AuthCredential;
import com.greedy.mokkoji.api.auth.dto.TokenPair;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import com.greedy.mokkoji.db.user.repository.RedisRepository;
import com.greedy.mokkoji.enums.auth.AuthRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 14; // 14일
    private static final String REFRESH_TOKEN_KEY_PREFIX = "refreshToken";
    private static final String KEY_DELIMITER = ":";

    private final RedisRepository redisRepository;
    private final JwtUtil jwtUtil;

    public TokenPair issueTokens(final AuthRole authRole, final Long userId) {
        final AuthCredential credential = new AuthCredential(authRole, userId);
        final String accessToken = jwtUtil.generateAccessToken(credential);
        final String refreshToken = jwtUtil.generateRefreshToken(credential);
        saveRefreshToken(authRole, userId, refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    private void saveRefreshToken(final AuthRole authRole, final Long userId, final String refreshToken) {
        redisRepository.save(
                refreshTokenKey(authRole, userId),
                refreshToken,
                REFRESH_TOKEN_EXPIRATION
        );
    }

    public String getRefreshToken(final AuthRole authRole, final Long userId) {
        return redisRepository.find(refreshTokenKey(authRole, userId));
    }

    public void deleteRefreshToken(final AuthRole authRole, final Long userId) {
        redisRepository.delete(refreshTokenKey(authRole, userId));
    }

    private String refreshTokenKey(final AuthRole authRole, final Long userId) {
        return REFRESH_TOKEN_KEY_PREFIX
                + KEY_DELIMITER
                + authRole.name()
                + KEY_DELIMITER
                + userId;
    }
}
