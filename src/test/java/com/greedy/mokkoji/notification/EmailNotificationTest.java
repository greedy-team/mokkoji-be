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

import org.springframework.mail.MailSendException;

import java.util.Map;

import static org.mockito.Mockito.*;
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

    @Test
    @DisplayName("전달된 payload가 없으면 메일을 발송하지 않는다")
    void 전달된_payload가_없으면_메일을_발송하지_않는다() {
        // when
        recruitmentNotificationEmailChannel.sendBatchNotification(List.of());

        // then
        BDDMockito.verify(mailSender, never()).send(any(MimeMessage[].class));
    }

    @Test
    @DisplayName("예기치 않은 예외 발생 시 모든 payload에 Discord 알림을 보낸다")
    void 예기치_않은_예외_발생_시_모든_payload에_Discord_알림을_보낸다() {
        // given
        RecruitmentMailPayload payload1 = new RecruitmentMailPayload(
                1L, "동아리A", List.of("a@test.com"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7)
        );
        RecruitmentMailPayload payload2 = new RecruitmentMailPayload(
                2L, "동아리B", List.of("b@test.com"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7)
        );

        BDDMockito.when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        BDDMockito.willThrow(new RuntimeException("unexpected smtp error"))
                .given(mailSender).send(any(MimeMessage[].class));

        // when
        recruitmentNotificationEmailChannel.sendBatchNotification(List.of(payload1, payload2));

        // then — 빌드 성공한 2개 payload 모두에 Discord 에스컬레이션
        BDDMockito.verify(discordNotifier, times(2))
                .notifyRecruitmentNotificationEmailFailure(any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("MailSendException 발생 시 실패한 메시지에만 Discord 알림을 보낸다")
    void MailSendException_발생_시_실패한_메시지에만_Discord_알림을_보낸다() {
        // given
        RecruitmentMailPayload payload1 = new RecruitmentMailPayload(
                1L, "동아리A", List.of("a@test.com"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7)
        );
        RecruitmentMailPayload payload2 = new RecruitmentMailPayload(
                2L, "동아리B", List.of("b@test.com"),
                LocalDateTime.now(), LocalDateTime.now().plusDays(7)
        );

        BDDMockito.when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage((Session) null));

        // varargs send()에서 첫 번째 인수(첫 번째 MimeMessage)만 실패로 주입
        BDDMockito.willAnswer(inv -> {
            MimeMessage firstMsg = (MimeMessage) inv.getArguments()[0];
            throw new MailSendException(Map.of(firstMsg, new Exception("send failed")));
        }).given(mailSender).send(any(MimeMessage[].class));

        // when
        recruitmentNotificationEmailChannel.sendBatchNotification(List.of(payload1, payload2));

        // then — 2개 중 첫 번째만 실패 → Discord 알림 1번
        BDDMockito.verify(discordNotifier, times(1))
                .notifyRecruitmentNotificationEmailFailure(any(), any(), anyInt(), any());
    }
}
