package com.greedy.mokkoji.api.jwt;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BearerAuthExtractor {
    private static final String BEARER_TYPE = "Bearer";

    public String extractTokenValue(final String bearerToken) {
        validateTokenValue(bearerToken);

        validateBearerKey(bearerToken);

        return bearerToken.substring(BEARER_TYPE.length()).trim();
    }

    private void validateTokenValue(final String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty() || bearerToken.trim().equals(BEARER_TYPE)) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_EMPTY_HEADER);
        }
    }

    private void validateBearerKey(final String bearerToken) {
        if (!bearerToken.toLowerCase().startsWith(BEARER_TYPE.toLowerCase())) {
            throw new MokkojiException(FailMessage.UNAUTHORIZED_INVALID_TOKEN);
        }
    }
}
