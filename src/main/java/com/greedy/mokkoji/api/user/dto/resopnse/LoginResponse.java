package com.greedy.mokkoji.api.user.dto.resopnse;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
    public static LoginResponse of(final String accessToken, final String refreshToken) {
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
