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
    private final JavaMailSender mailSender;
    private final DiscordNotifier discordNotifier;

    @Value("${spring.mail.username}")
    private String senderMail;
    @Value("${mokkoji.base-url}")
    private String baseUrl;

    private String generateHtmlText(
            final Long clubId,
            final String clubName,
            final LocalDateTime recruitStart,
            final LocalDateTime recruitEnd
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");

        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<title>동아리 모집 안내</title>" +
                "</head>" +
                "<body style='font-family: Arial, sans-serif; margin: 20px;'>" +
                "<h2 style='color: #2E86C1;'>🎉 " + clubName + " 모집 시작! 🎉</h2>" +
                "<p>안녕하세요!</p>" +
                "<p>모꼬지에서 즐겨찾기하신 <strong>" + clubName + "</strong> 동아리가 신규 회원을 모집합니다.</p>" +
                "<p>📅 <strong>모집 기간:</strong> " + recruitStart.format(formatter) + " ~ " + recruitEnd.format(formatter) + "</p>" +
                "<p>지금 바로 지원하여 기회를 놓치지 마세요!</p>" +
                "<a href='" + baseUrl + "/club/" + clubId + "' " +
                "style='display: inline-block; padding: 10px 20px; margin-top: 20px; font-size: 16px; color: white; background-color: #2E86C1; text-decoration: none; border-radius: 5px;'>신청하러 가기</a>" +
                "<p>감사합니다!<br>모꼬지 팀 드림</p>" +
                "</body>" +
                "</html>";
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

            final String[] receiverMailsS = receiverMails.toArray(String[]::new);
            helper.setTo(receiverMailsS);

            helper.setSubject(SUBJECT);

            final String text = generateHtmlText(clubId, clubName, recruitStartTime, recruitEndTime);
            helper.setText(text, true);
            mailSender.send(mimeMessage);
            throw new MessagingException();
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

