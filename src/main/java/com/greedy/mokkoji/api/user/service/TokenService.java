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

    public TokenPair issueTokens(final AuthRole authRole, final Long accountId) {
        final AuthCredential credential = new AuthCredential(authRole, accountId);
        final String accessToken = jwtUtil.generateAccessToken(credential);
        final String refreshToken = jwtUtil.generateRefreshToken(credential);
        saveRefreshToken(authRole, accountId, refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    private void saveRefreshToken(final AuthRole authRole, final Long accountId, final String refreshToken) {
        redisRepository.save(
                refreshTokenKey(authRole, accountId),
                refreshToken,
                REFRESH_TOKEN_EXPIRATION
        );
    }

    public String getRefreshToken(final AuthRole authRole, final Long accountId) {
        return redisRepository.find(refreshTokenKey(authRole, accountId));
    }

    public void deleteRefreshToken(final AuthRole authRole, final Long accountId) {
        redisRepository.delete(refreshTokenKey(authRole, accountId));
    }

    private String refreshTokenKey(final AuthRole authRole, final Long accountId) {
        return REFRESH_TOKEN_KEY_PREFIX
                + KEY_DELIMITER
                + authRole.name()
                + KEY_DELIMITER
                + accountId;
    }
}
