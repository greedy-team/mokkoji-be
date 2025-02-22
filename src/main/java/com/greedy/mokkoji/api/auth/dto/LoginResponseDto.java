package com.greedy.mokkoji.api.auth.dto;

public record LoginResponseDto(
        String accessToken,
        String refreshToken
) {
}
