package com.greedy.mokkoji.api.user.service;

import com.greedy.mokkoji.api.external.KakaoAccessTokenClient;
import com.greedy.mokkoji.api.external.KakaoUserInfoClient;
import com.greedy.mokkoji.api.user.dto.resopnse.KakaoAccessTokenResponse;
import com.greedy.mokkoji.api.user.dto.resopnse.KakaoUserInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoSocialLoginService {

    private static final String TOKEN_TYPE = "Bearer ";
    private final KakaoAccessTokenClient kakaoAccessTokenClient;
    private final KakaoUserInfoClient kakaoUserInfoClient;
    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${kakao.content-type}")
    private String kakaoContentType;
    @Value("${kakao.grant-type}")
    private String kakaoGrantType;

    public KakaoUserInfoResponse login(final String code) {
        final KakaoAccessTokenResponse kakaoAccessTokenResponse = kakaoAccessTokenClient.kakaoAuth(
                kakaoContentType,
                code,
                kakaoClientId,
                kakaoRedirectUri,
                kakaoGrantType
        );

        return kakaoUserInfoClient.kakaoUserInfo(
                TOKEN_TYPE + kakaoAccessTokenResponse.accessToken(),
                kakaoContentType
        );
    }
}
