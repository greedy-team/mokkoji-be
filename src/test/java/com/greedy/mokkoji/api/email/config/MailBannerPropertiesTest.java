package com.greedy.mokkoji.api.email.config;

import com.greedy.mokkoji.enums.university.UniversityCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("메일 배너 프로퍼티 바인딩 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MailBannerPropertiesTest {

    private static final String SEJONG_BANNER_URL = "https://cdn.mokkoji.site/email-image/sejong.png";
    private static final String KONKUK_BANNER_URL = "https://cdn.mokkoji.site/email-image/konkuk.png";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(MailBannerProperties.class)
    static class TestConfig {
    }

    @Test
    @DisplayName("소문자 yml 키가 enum 키로 바인딩된다")
    void bindLowercaseKeyTest() {
        contextRunner
                .withPropertyValues(
                        "mokkoji.mail.banner-urls.sejong=" + SEJONG_BANNER_URL,
                        "mokkoji.mail.banner-urls.konkuk=" + KONKUK_BANNER_URL
                )
                .run(context -> {
                    MailBannerProperties properties = context.getBean(MailBannerProperties.class);
                    assertThat(properties.bannerUrls()).containsEntry(UniversityCode.SEJONG, SEJONG_BANNER_URL);
                    assertThat(properties.bannerUrlOf(UniversityCode.SEJONG)).contains(SEJONG_BANNER_URL);
                    assertThat(properties.bannerUrlOf(UniversityCode.KONKUK)).contains(KONKUK_BANNER_URL);
                });
    }

    @Test
    @DisplayName("대문자 yml 키도 enum 키로 바인딩된다")
    void bindUppercaseKeyTest() {
        contextRunner
                .withPropertyValues("mokkoji.mail.banner-urls.SEJONG=" + SEJONG_BANNER_URL)
                .run(context -> {
                    MailBannerProperties properties = context.getBean(MailBannerProperties.class);
                    assertThat(properties.bannerUrlOf(UniversityCode.SEJONG)).contains(SEJONG_BANNER_URL);
                });
    }

    @Test
    @DisplayName("enum에 없는 오타 키는 기동 시점에 바인딩 실패한다")
    void bindTypoKeyFailsFastTest() {
        contextRunner
                .withPropertyValues("mokkoji.mail.banner-urls.sejongg=" + SEJONG_BANNER_URL)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("banner-urls 블록이 없으면 빈 맵으로 기동된다")
    void bindMissingBlockTest() {
        contextRunner.run(context -> {
            MailBannerProperties properties = context.getBean(MailBannerProperties.class);
            assertThat(properties.bannerUrls()).isEmpty();
            assertThat(properties.bannerUrlOf(UniversityCode.SEJONG)).isEmpty();
        });
    }

    @Test
    @DisplayName("빈 문자열 값은 배너 없음으로 처리된다")
    void blankValueTest() {
        MailBannerProperties properties = new MailBannerProperties(
                Map.of(UniversityCode.SEJONG, SEJONG_BANNER_URL, UniversityCode.KONKUK, "")
        );

        assertThat(properties.bannerUrlOf(UniversityCode.SEJONG)).contains(SEJONG_BANNER_URL);
        assertThat(properties.bannerUrlOf(UniversityCode.KONKUK)).isEmpty();
        assertThat(properties.bannerUrlOf(UniversityCode.HANYANG)).isEmpty();
    }
}
