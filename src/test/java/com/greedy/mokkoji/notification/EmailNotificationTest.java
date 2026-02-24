package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.notification.service.EmailNotificationChannel;
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
    private EmailNotificationChannel emailNotificationChannel;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        // TODO:: 다른 방법 생각해보기
        ReflectionTestUtils.setField(emailNotificationChannel, "senderMail", "test@mokkoji.com");
    }

    @Test
    @DisplayName("이메일 알림이 발송된다")
    void sendNotificationTest() {
        // given
        final List<String> receiverMails = List.of("test@test.com");
        final Long clubId = 1L;
        final String clubName = "테스트";
        final LocalDateTime recruitStart = LocalDateTime.now();
        final LocalDateTime recruitEnd = LocalDateTime.now().plusDays(7);

        final MimeMessage mimeMessage = new MimeMessage((Session) null);
        BDDMockito.when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        BDDMockito.doNothing().when(mailSender).send(any(MimeMessage.class));

        // when
        emailNotificationChannel.sendNotification(receiverMails, clubId, clubName, recruitStart, recruitEnd);

        // then
        BDDMockito.verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
