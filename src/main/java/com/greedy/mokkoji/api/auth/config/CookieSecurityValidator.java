package com.greedy.mokkoji.api.auth.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * secure 가 빠진 쿠키는 토큰이 평문 노출되지만 동작은 정상으로 보여 운영 중 발견이 어렵다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CookieSecurityValidator {

    private static final List<String> LOCAL_PROFILES = List.of("local", "test");

    private final CookieProperties cookieProperties;
    private final Environment environment;

    @PostConstruct
    public void validate() {
        if (cookieProperties.secure()) {
            return;
        }

        if (isLocalProfile()) {
            log.warn("auth.cookie.secure=false 로 실행됩니다. 로컬 개발 환경에서만 허용됩니다.");
            return;
        }

        throw new IllegalStateException(
                "auth.cookie.secure=false 는 로컬 환경에서만 허용됩니다. "
                        + "현재 활성 프로파일: " + Arrays.toString(environment.getActiveProfiles())
        );
    }

    private boolean isLocalProfile() {
        final String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return true;
        }
        return Arrays.stream(activeProfiles).allMatch(LOCAL_PROFILES::contains);
    }
}
