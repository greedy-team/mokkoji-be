package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.config.MailBannerProperties;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.email.service.RecruitmentNotificationEmailChannel;
import com.greedy.mokkoji.enums.university.UniversityCode;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 알림 채널 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EmailNotificationTest {

    private static final String BASE_URL = "https://www.mokkoji.site";
    private static final String SEJONG_BANNER_URL = "https://cdn.mokkoji.site/email-image/EmailBanner.png";

    private RecruitmentNotificationEmailChannel recruitmentNotificationEmailChannel;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private DiscordNotifier discordNotifier;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        final MailBannerProperties mailBannerProperties = new MailBannerProperties(
                Map.of(UniversityCode.SEJONG, SEJONG_BANNER_URL, UniversityCode.KONKUK, "")
        );
        recruitmentNotificationEmailChannel =
                new RecruitmentNotificationEmailChannel(mailSender, discordNotifier, mailBannerProperties);

        // TODO:: 다른 방법 생각해보기
        ReflectionTestUtils.setField(recruitmentNotificationEmailChannel, "senderMail", "test@mokkoji.com");
        ReflectionTestUtils.setField(recruitmentNotificationEmailChannel, "baseUrl", BASE_URL);

        mimeMessage = new MimeMessage((Session) null);
        BDDMockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        BDDMockito.doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("이메일 알림이 발송된다")
    void sendNotificationTest() {
        // when
        sendNotification(UniversityCode.SEJONG);

        // then
        BDDMockito.verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("신청하러 가기 링크에 동아리가 속한 학교 코드가 포함된다")
    void clubDetailLinkContainsUniversityCode() throws Exception {
        // when
        sendNotification(UniversityCode.KONKUK);

        // then
        final String html = extractHtml();
        assertThat(html).contains("href=\"" + BASE_URL + "/konkuk/club/1\"");
        assertThat(html).doesNotContain("/sejong/");
    }

    @Test
    @DisplayName("학교별 배너가 설정되어 있으면 해당 배너를 사용한다")
    void bannerOfUniversityIsUsed() throws Exception {
        // when
        sendNotification(UniversityCode.SEJONG);

        // then
        assertThat(extractHtml()).contains("<img src=\"" + SEJONG_BANNER_URL + "\"");
    }

    @Test
    @DisplayName("학교별 배너가 없으면 다른 학교 배너를 쓰지 않고 배너를 생략한다")
    void bannerIsOmittedWhenNotConfigured() throws Exception {
        // when
        sendNotification(UniversityCode.HANYANG);

        // then
        final String html = extractHtml();
        assertThat(html).doesNotContain("<img");
        assertThat(html).doesNotContain(SEJONG_BANNER_URL);
    }

    private void sendNotification(final UniversityCode universityCode) {
        recruitmentNotificationEmailChannel.sendNotification(
                List.of("test@test.com"),
                1L,
                "테스트",
                universityCode,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
    }

    private String extractHtml() throws Exception {
        mimeMessage.saveChanges();
        final jakarta.mail.Multipart multipart = (jakarta.mail.Multipart) mimeMessage.getContent();
        return findHtml(multipart);
    }

    private String findHtml(final jakarta.mail.Multipart multipart) throws Exception {
        for (int i = 0; i < multipart.getCount(); i++) {
            final jakarta.mail.BodyPart part = multipart.getBodyPart(i);
            final Object content = part.getContent();
            if (content instanceof jakarta.mail.Multipart nested) {
                return findHtml(nested);
            }
            if (part.isMimeType("text/html")) {
                return (String) content;
            }
        }
        throw new AssertionError("text/html 파트를 찾지 못했습니다");
    }
}
