package com.greedy.mokkoji.api.user.service.kakao;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoRedirectUriResolver {

    private final String callbackPath;

    public KakaoRedirectUriResolver(@Value("${kakao.callback-path}") final String callbackPath) {
        this.callbackPath = callbackPath;
    }

    public String resolve(final String origin) {
        if (origin == null || origin.isBlank()) {
            throw new MokkojiException(FailMessage.BAD_REQUEST_MISSING_PARAM);
        }
        return origin + callbackPath;
    }
}
