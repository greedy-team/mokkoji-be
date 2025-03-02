package com.greedy.mokkoji.api.jwt;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BearerAuthExtractor {
    private static final String BEARER_TYPE = "Bearer";

    public String extractTokenValue(final String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER);
        }

        if (!bearerToken.toLowerCase().startsWith(BEARER_TYPE.toLowerCase())) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
        }

        return bearerToken.substring(BEARER_TYPE.length()).trim();
    }
}
