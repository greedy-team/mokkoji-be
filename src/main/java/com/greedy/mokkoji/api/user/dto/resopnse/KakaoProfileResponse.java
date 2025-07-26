package com.greedy.mokkoji.api.user.dto.resopnse;

public record KakaoProfileResponse(
        String nickname,
        boolean isDefaultNickname
) {
}
