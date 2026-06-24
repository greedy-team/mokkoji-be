package com.greedy.mokkoji.user.service.kakao;

import com.greedy.mokkoji.api.user.service.kakao.KakaoRedirectUriResolver;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("카카오 redirect_uri Resolver 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class KakaoRedirectUriResolverTest {

    private static final String CALLBACK_PATH = "/api/auth/callback/kakao";

    private final KakaoRedirectUriResolver resolver = new KakaoRedirectUriResolver(CALLBACK_PATH);

    @Test
    @DisplayName("Origin 헤더와 콜백 경로를 합쳐 redirect_uri를 만든다.")
    void resolveSuccessful() {
        final String origin = "http://localhost:3000";

        final String redirectUri = resolver.resolve(origin);

        assertThat(redirectUri).isEqualTo("http://localhost:3000/api/auth/callback/kakao");
    }

    @Test
    @DisplayName("Origin이 null이면 BAD_REQUEST_MISSING_PARAM 예외를 던진다.")
    void resolveThrowsWhenOriginNull() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(MokkojiException.class)
                .hasFieldOrPropertyWithValue("failMessage", FailMessage.BAD_REQUEST_MISSING_PARAM);
    }

    @Test
    @DisplayName("Origin이 공백이면 BAD_REQUEST_MISSING_PARAM 예외를 던진다.")
    void resolveThrowsWhenOriginBlank() {
        assertThatThrownBy(() -> resolver.resolve("   "))
                .isInstanceOf(MokkojiException.class)
                .hasFieldOrPropertyWithValue("failMessage", FailMessage.BAD_REQUEST_MISSING_PARAM);
    }
}
