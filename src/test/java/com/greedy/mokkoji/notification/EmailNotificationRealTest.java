package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.service.RecruitmentNotificationEmailChannel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@DisplayName("실제 이메일 발송 테스트")
@Disabled("이메일 테스트 시 해제를 하고 사용")
public class EmailNotificationRealTest {

    @Value("${test.mail.receivers}")
    private String receivers;

    @Autowired
    private RecruitmentNotificationEmailChannel recruitmentNotificationEmailChannel;

    @Test
    @DisplayName("수신자 이메일 노출 확인")
    void checkEmailExposure() {

        List<String> receiverMails = Arrays.stream(receivers.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        recruitmentNotificationEmailChannel.sendNotification(
                receiverMails,
                1L,
                "수신자 노출 테스트",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(7)
        );
    }
}
