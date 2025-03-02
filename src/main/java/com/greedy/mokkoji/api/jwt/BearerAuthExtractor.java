package com.greedy.mokkoji.api.jwt;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BearerAuthExtractor {
    private static final String BEARER_TYPE = "Bearer";

    public String extractTokenValue(final String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            log.warn("Autherizaiton 헤더 값이 없습니다.");
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER);
        }

        if (!bearerToken.toLowerCase().startsWith(BEARER_TYPE.toLowerCase())) {
            log.warn("AuthHeader의 값이 Bearer로 시작하지 않습니다: {}", bearerToken);
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
        }

        return bearerToken.substring(BEARER_TYPE.length()).trim();
    }
}
