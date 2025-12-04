package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RefreshResponse(
        @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken
) {

    public static RefreshResponse of(final String accessToken) {
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
