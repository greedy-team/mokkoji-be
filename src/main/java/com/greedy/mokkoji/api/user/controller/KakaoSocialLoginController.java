package com.greedy.mokkoji.api.user.controller;

import com.greedy.mokkoji.api.user.dto.request.KakaoSocialLoginRequest;
import com.greedy.mokkoji.api.user.dto.resopnse.KakaoUserInfoResponse;
import com.greedy.mokkoji.api.user.service.KakaoSocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/kakao")
public class KakaoSocialLoginController {

    private final KakaoSocialLoginService kakaSocialLoginService;

    @PostMapping("/login/openfeign")
    public KakaoUserInfoResponse login(
        @RequestBody KakaoSocialLoginRequest kakaoSocialLoginRequest
    ) {
        return kakaSocialLoginService.login(kakaoSocialLoginRequest.code());
    }
}
