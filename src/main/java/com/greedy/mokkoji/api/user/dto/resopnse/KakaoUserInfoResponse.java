package com.greedy.mokkoji.api.user.dto.resopnse;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserInfoResponse(
        String id,
        KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
            Profile profile
    ) {
        public record Profile(
                String nickname,
                boolean isDefaultNickname
        ) {}
    }
}

