package com.greedy.mokkoji.api.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
