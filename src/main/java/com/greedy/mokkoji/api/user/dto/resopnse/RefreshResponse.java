package com.greedy.mokkoji.api.user.dto.resopnse;

import lombok.Builder;

@Builder
public record RefreshResponse(
        String accessToken
) {

    public static RefreshResponse of(final String accessToken) {
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
