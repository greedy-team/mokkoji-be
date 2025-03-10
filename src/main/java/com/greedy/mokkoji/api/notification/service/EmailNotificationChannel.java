package com.greedy.mokkoji.api.notification.service;

import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.enums.message.FailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {
    private static final String SUBJECT = "동아리 모집 시작";
    private static final String SENDER_NAME = "모꼬지";
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String senderMail;

    private String generateText(
            final String clubName,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd
    ) {
        return "모꼬지를 서비스에서 즐겨찾기 해주신 " + clubName + "이 동아리 모집을 시작했습니다!\n"
                + "모집 기간은" + recruitStart + "부터 " + recruitEnd + "까지 입니다!.";
    }

    private MimeMessage generateNotification(
            final List<String> receiverMails,
            final String clubName,
            final LocalDateTime recruitStartTime,
            final LocalDateTime recruitEndTime
    ) {
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(senderMail, SENDER_NAME);

            final String[] receiverMailsS = receiverMails.toArray(String[]::new);
            helper.setTo(receiverMailsS);

            helper.setSubject(SUBJECT);

            final String text = generateText(clubName, recruitStartTime, recruitEndTime);
            helper.setText(text);

            return mimeMessage;
        } catch (MessagingException e) {
            log.error("[MAIL GENERATING ERROR]: {}", e.getMessage());
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SMTP_MAIL);
        } catch (UnsupportedEncodingException e) {
            log.error("[Mail GENERATING ERROR FROM SENDER INFORMATION]: {}", e.getMessage());
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SMTP_MAIL);
        }
    }

    @Override
    public void sendNotification(
            final List<String> receiverMails,
            final String clubName,
            final LocalDateTime recruitStartTime,
            final LocalDateTime recruitEndTime
    ) {
        try {
            final MimeMessage mimeMessage = generateNotification(receiverMails, clubName, recruitStartTime, recruitEndTime);

            mailSender.send(mimeMessage);
        } catch (MailException e) {
            log.error("[MAIL SEND FAILED] : {}", e.getMessage());
            throw new MokkojiException(FailMessage.INTERNAL_SERVER_ERROR_SMTP);
        }
    }
}
