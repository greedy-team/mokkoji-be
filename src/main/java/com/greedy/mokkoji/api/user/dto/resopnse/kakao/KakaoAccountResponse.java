package com.greedy.mokkoji.api.user.dto.resopnse.kakao;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoAccountResponse (
        KakaoProfileResponse profile
) {
}
