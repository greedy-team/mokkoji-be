package com.greedy.mokkoji.auth;

import com.greedy.mokkoji.api.auth.config.CookieProperties;
import com.greedy.mokkoji.api.auth.service.AuthCookieFactory;
import com.greedy.mokkoji.api.jwt.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("인증 쿠키 속성")
class AuthCookieFactoryTest {

    private AuthCookieFactory factoryWith(final String domain, final boolean secure) {
        return new AuthCookieFactory(new CookieProperties(domain, secure));
    }

    @Test
    void access_token_쿠키는_HttpOnly_Secure_SameSite_Lax_속성을_가진다() {
        final ResponseCookie cookie = factoryWith(".mokkoji.site", true).accessToken("token");

        assertThat(cookie.getName()).isEqualTo("access_token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getDomain()).isEqualTo(".mokkoji.site");
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    void access_token_쿠키의_수명은_access_토큰_수명과_같다() {
        final ResponseCookie cookie = factoryWith(".mokkoji.site", true).accessToken("token");

        assertThat(cookie.getMaxAge())
                .isEqualTo(Duration.ofMillis(JwtUtil.ACCESS_TOKEN_EXPIRATION));
    }

    @Test
    void refresh_token_쿠키의_수명은_refresh_토큰_수명과_같다() {
        final ResponseCookie cookie = factoryWith(".mokkoji.site", true).refreshToken("token");

        assertThat(cookie.getName()).isEqualTo("refresh_token");
        assertThat(cookie.getMaxAge())
                .isEqualTo(Duration.ofMillis(JwtUtil.REFRESH_TOKEN_EXPIRATION));
    }

    @Test
    void secure가_false면_Secure_속성이_붙지_않는다() {
        final ResponseCookie cookie = factoryWith(null, false).accessToken("token");

        assertThat(cookie.isSecure()).isFalse();
    }

    @Test
    void domain이_비어있으면_host_only_쿠키가_된다() {
        final ResponseCookie cookie = factoryWith("", false).accessToken("token");

        assertThat(cookie.getDomain()).isNull();
    }

    @Test
    void 만료_쿠키는_빈_값과_0초_수명을_가진다() {
        final AuthCookieFactory factory = factoryWith(".mokkoji.site", true);

        assertThat(factory.expiredAccessToken().getValue()).isEmpty();
        assertThat(factory.expiredAccessToken().getMaxAge()).isZero();
        assertThat(factory.expiredRefreshToken().getValue()).isEmpty();
        assertThat(factory.expiredRefreshToken().getMaxAge()).isZero();
    }

    @Test
    void 만료_쿠키는_발급_때와_같은_Domain과_Path를_가진다() {
        final AuthCookieFactory factory = factoryWith(".mokkoji.site", true);
        final ResponseCookie issued = factory.accessToken("token");
        final ResponseCookie expired = factory.expiredAccessToken();

        assertThat(expired.getDomain()).isEqualTo(issued.getDomain());
        assertThat(expired.getPath()).isEqualTo(issued.getPath());
    }
}
