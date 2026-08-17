package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import com.greedy.mokkoji.api.email.service.RecruitmentMailPayload;
import com.greedy.mokkoji.api.email.service.RecruitmentNotificationEmailChannel;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("이메일 알림 채널 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class EmailNotificationTest {
    @InjectMocks
    private RecruitmentNotificationEmailChannel recruitmentNotificationEmailChannel;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private DiscordNotifier discordNotifier;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(recruitmentNotificationEmailChannel, "senderMail", "test@mokkoji.com");
        ReflectionTestUtils.setField(recruitmentNotificationEmailChannel, "baseUrl", "https://mokkoji.com");
        ReflectionTestUtils.setField(recruitmentNotificationEmailChannel, "mailBannerUrl", "https://mokkoji.com/banner.png");
    }

    @Test
    @DisplayName("이메일 알림이 발송된다")
    void sendNotificationTest() {
        // given
        RecruitmentMailPayload payload = new RecruitmentMailPayload(
                1L,
                "테스트",
                List.of("test@test.com"),
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );

        MimeMessage mimeMessage = new MimeMessage((Session) null);
        BDDMockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        BDDMockito.doNothing().when(mailSender).send(any(MimeMessage[].class));

        // when
        recruitmentNotificationEmailChannel.sendBatchNotification(List.of(payload));

        // then
        BDDMockito.verify(mailSender, times(1)).send(any(MimeMessage[].class));
    }
}
