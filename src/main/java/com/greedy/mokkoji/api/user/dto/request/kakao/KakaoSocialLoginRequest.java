package com.greedy.mokkoji.api.user.dto.request.kakao;

import com.greedy.mokkoji.enums.university.UniversityCode;

public record KakaoSocialLoginRequest(
        String code, UniversityCode universityCode
) {
}
