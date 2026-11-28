package com.greedy.mokkoji.auth;

import com.greedy.mokkoji.api.auth.config.CsrfProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("CSRF 허용 Origin 목록")
class CsrfPropertiesTest {

    @Test
    void 콤마_뒤_공백이_있어도_허용된_출처로_인식한다() {
        final CsrfProperties properties =
                new CsrfProperties(List.of("http://localhost:3000", " http://localhost:5173"));

        assertThat(properties.isAllowed("http://localhost:5173")).isTrue();
    }

    @Test
    void 목록에_없는_출처는_거부한다() {
        final CsrfProperties properties =
                new CsrfProperties(List.of("http://localhost:3000"));

        assertThat(properties.isAllowed("https://evil.example.com")).isFalse();
    }

    @Test
    void 빈_항목은_목록에서_제외한다() {
        final CsrfProperties properties =
                new CsrfProperties(List.of("http://localhost:3000", "  ", ""));

        assertThat(properties.isAllowed("")).isFalse();
        assertThat(properties.isAllowed("  ")).isFalse();
    }

    @Test
    void 설정이_없으면_어떤_출처도_허용하지_않는다() {
        final CsrfProperties properties = new CsrfProperties(null);

        assertThat(properties.isAllowed("http://localhost:3000")).isFalse();
    }
}
