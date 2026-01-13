package com.greedy.mokkoji.api.notification.service;

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
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.greedy.mokkoji.enums.message.FailMessage.INTERNAL_SERVER_ERROR;
import static com.greedy.mokkoji.enums.message.FailMessage.INTERNAL_SERVER_ERROR_SMTP;
import static com.greedy.mokkoji.enums.message.FailMessage.INTERNAL_SERVER_ERROR_SMTP_MAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {
    private static final String SUBJECT = "동아리 모집 시작";
    private static final String SENDER_NAME = "모꼬지";
    private static final String OFFICIAL_EMAIL = "noreply@mokkoji.com";
    private final JavaMailSender mailSender;
    private final DiscordNotifier discordNotifier;

    @Value("${spring.mail.username}")
    private String senderMail;
    @Value("${mokkoji.base-url}")
    private String baseUrl;
    @Value("${mokkoji.mail.banner-url}")
    private String mailBannerUrl;

    private String generateHtmlText(
            final Long clubId,
            final String clubName,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>동아리 모집 안내</title>
                </head>
                <body style="font-family: Arial, sans-serif;">
                
                    <!-- 배너 이미지 -->
                    <img src="%s"
                         alt="모꼬지 배너"
                         style="width: 100%%; display: block; margin: 0 0 20px 0; max-width: 600px;" />
                    <div style="margin: 40px">
                    <h3>📣 %s 모집 안내!</h3>
                    <p style="margin: 0 0 8px 0;">안녕하세요! <strong>모꼬지</strong>입니다:)</p>
                    <p style="margin: 0 0 30px 0;">즐겨찾기하신 <strong>%s</strong> 동아리가 신규 회원을 모집합니다.</p>
                    <p style="margin: 0 0 8px 0;"><strong>모집 기간 : %s ~ %s</strong></p>
                    <p style="margin: 0 0 30px 0;">지금 바로 지원하여 기회를 놓치지 마세요!</p>
                    <p style="margin: 0 0 30px 0;">모꼬지 드림.</p>
                
                    <a href="%s/club/%d"
                       style="display: inline-block; padding: 10px 15px;
                              font-size: 14px; color: #000000; background-color: #4AF38A;
                              text-decoration: none; border-radius: 40px; font-weight: 500;">
                       신청하러 가기
                    </a>
                    </div>
                </body>
                </html>
                """.formatted(
                mailBannerUrl,
                clubName,
                clubName,
                recruitStart.format(formatter),
                recruitEnd.format(formatter),
                baseUrl,
                clubId
        );

    }

    @Override
    public void sendNotification(
            final List<String> receiverMails,
            final Long clubId,
            final String clubName,
            final LocalDateTime recruitStartTime,
            final LocalDateTime recruitEndTime
    ) {
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(senderMail, SENDER_NAME);
            helper.setTo(OFFICIAL_EMAIL);

            final String[] receiverMailsS = receiverMails.toArray(String[]::new);
            helper.setBcc(receiverMailsS);

            helper.setSubject(SUBJECT);

            final String text = generateHtmlText(clubId, clubName, recruitStartTime, recruitEndTime);
            helper.setText(text, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("[MAIL GENERATING ERROR] clubId={} clubName={} receivers={} message={}",
                    clubId,
                    clubName,
                    receiverMails,
                    e.getMessage());
            discordNotifier.notifyEmailFailure(clubId, clubName, receiverMails.size(), INTERNAL_SERVER_ERROR_SMTP_MAIL.getMessage());
        } catch (MailException e) {
            log.error("[MAIL SEND FAILED] clubId={} clubName={} receivers={} message={}",
                    clubId,
                    clubName,
                    receiverMails,
                    e.getMessage());
            discordNotifier.notifyEmailFailure(clubId, clubName, receiverMails.size(), INTERNAL_SERVER_ERROR_SMTP.getMessage());
        } catch (Exception e) {
            log.error("[EMAIL UNEXPECTED ERROR] clubId={}, clubName={} receivers={}",
                    clubId,
                    clubName,
                    receiverMails,
                    e);
            discordNotifier.notifyEmailFailure(clubId, clubName, receiverMails.size(), INTERNAL_SERVER_ERROR.getMessage());
        }
    }
}

