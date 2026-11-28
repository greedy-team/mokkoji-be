package com.greedy.mokkoji.auth;

import com.greedy.mokkoji.api.auth.config.CookieProperties;
import com.greedy.mokkoji.api.auth.config.CookieSecurityValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayName("쿠키 secure 설정 검증")
class CookieSecurityValidatorTest {

    private CookieSecurityValidator validator(final boolean secure, final String... profiles) {
        final MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles(profiles);
        return new CookieSecurityValidator(new CookieProperties(".mokkoji.site", secure), environment);
    }

    @Test
    void 운영_프로파일에서_secure가_false면_기동에_실패한다() {
        assertThatThrownBy(() -> validator(false, "prod").validate())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("secure");
    }

    @Test
    void dev_프로파일에서_secure가_false면_기동에_실패한다() {
        assertThatThrownBy(() -> validator(false, "dev").validate())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 로컬_프로파일에서는_secure가_false여도_기동한다() {
        assertThatCode(() -> validator(false, "local").validate())
                .doesNotThrowAnyException();
    }

    @Test
    void 테스트_프로파일에서는_secure가_false여도_기동한다() {
        assertThatCode(() -> validator(false, "test").validate())
                .doesNotThrowAnyException();
    }

    @Test
    void 프로파일이_없으면_로컬로_보고_기동한다() {
        assertThatCode(() -> validator(false).validate())
                .doesNotThrowAnyException();
    }

    @Test
    void secure가_true면_어떤_프로파일이든_기동한다() {
        assertThatCode(() -> validator(true, "prod").validate())
                .doesNotThrowAnyException();
        assertThatCode(() -> validator(true, "local").validate())
                .doesNotThrowAnyException();
    }

    @Test
    void 로컬과_운영_프로파일이_함께_활성화되면_기동에_실패한다() {
        assertThatThrownBy(() -> validator(false, "local", "prod").validate())
                .isInstanceOf(IllegalStateException.class);
    }
}
