package com.greedy.mokkoji.api.user.dto.resopnse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "토큰 갱신 응답")
public record RefreshResponse(
        @Schema(description = "새로운 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken
) {

    public static RefreshResponse of(final String accessToken) {
        return RefreshResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
