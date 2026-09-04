package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.api.email.config.MailBannerProperties;
import com.greedy.mokkoji.enums.university.UniversityCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.greedy.mokkoji.enums.message.FailMessage.*;

@Slf4j
@Component
public class RecruitmentNotificationEmailChannel extends AbstractEmailSender implements RecruitmentNotificationChannel {

    private static final String SUBJECT_SUFFIX = " 모집 안내";

    private final MailBannerProperties mailBannerProperties;

    public RecruitmentNotificationEmailChannel(
            JavaMailSender mailSender,
            DiscordNotifier discordNotifier,
            MailBannerProperties mailBannerProperties
    ) {
        super(mailSender, discordNotifier);
        this.mailBannerProperties = mailBannerProperties;
    }

    @Async("emailExecutor")
    @Override
    public void sendBatchNotification(List<RecruitmentMailPayload> payloads) {
        List<MimeMessage> messages = new ArrayList<>();
        List<RecruitmentMailPayload> builtPayloads = new ArrayList<>();

        for (RecruitmentMailPayload payload : payloads) {
            try {
                String subject = payload.clubName() + SUBJECT_SUFFIX;
                String html = generateHtmlText(payload.clubId(), payload.clubName(), payload.universityCode(), payload.recruitStart(), payload.recruitEnd());
                messages.add(buildMimeMessage(subject, html, payload.receiverMails()));
                builtPayloads.add(payload);
            } catch (MessagingException | UnsupportedEncodingException e) {
                log.error("[MAIL GENERATING ERROR] clubId={} message={}", payload.clubId(), e.getMessage(), e);
                discordNotifier.notifyRecruitmentNotificationEmailFailure(
                        payload.clubId(), payload.clubName(), payload.receiverMails().size(),
                        INTERNAL_SERVER_ERROR_SMTP_MAIL.getMessage()
                );
            }
        }

        if (messages.isEmpty()) {
            return;
        }

        try {
            mailSender.send(messages.toArray(new MimeMessage[0]));
        } catch (MailSendException e) {
            Map<Object, Exception> failedMessages = e.getFailedMessages();
            for (int i = 0; i < messages.size(); i++) {
                if (failedMessages.containsKey(messages.get(i))) {
                    RecruitmentMailPayload payload = builtPayloads.get(i);
                    log.error("[MAIL SEND FAILED] clubId={} message={}", payload.clubId(), e.getMessage(), e);
                    discordNotifier.notifyRecruitmentNotificationEmailFailure(
                            payload.clubId(), payload.clubName(), payload.receiverMails().size(),
                            INTERNAL_SERVER_ERROR_SMTP.getMessage()
                    );
                }
            }
        } catch (Exception e) {
            log.error("[EMAIL UNEXPECTED ERROR]", e);
            builtPayloads.forEach(payload ->
                    discordNotifier.notifyRecruitmentNotificationEmailFailure(
                            payload.clubId(), payload.clubName(), payload.receiverMails().size(),
                            INTERNAL_SERVER_ERROR.getMessage()
                    )
            );
        }
    }

    private String generateHtmlText(
            Long clubId,
            String clubName,
            UniversityCode universityCode,
            LocalDateTime recruitStart,
            LocalDateTime recruitEnd
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일");
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif;">
                    %s
                    <div style="margin: 40px">
                        <h3>📣 %s 모집 안내!</h3>
                        <p>안녕하세요! <strong>모꼬지</strong>입니다:)</p>
                        <p>즐겨찾기하신 <strong>%s</strong> 동아리가 신규 회원을 모집합니다.</p>
                        <p><strong>모집 기간 : %s ~ %s</strong></p>
                        <p>지금 바로 지원하여 기회를 놓치지 마세요!</p>
                        <p>모꼬지 드림.</p>
                        <a href="%s" style="display: inline-block; padding: 10px 15px; font-size: 14px; color: #000000; background-color: #4AF38A; text-decoration: none; border-radius: 40px; font-weight: 500;"><strong>신청하러 가기</strong></a>
                    </div>
                </body>
                </html>
                """.formatted(
                generateBannerTag(universityCode),
                clubName,
                clubName,
                recruitStart.format(formatter),
                recruitEnd.format(formatter),
                generateClubDetailUrl(universityCode, clubId)
        );
    }

    private String generateBannerTag(UniversityCode universityCode) {
        return mailBannerProperties.bannerUrlOf(universityCode)
                .map(url -> "<img src=\"%s\" alt=\"모꼬지 배너\" style=\"width: 100%%; max-width: 600px; display: block; margin: 0 0 20px 0;\" />"
                        .formatted(url))
                .orElse("");
    }

    private String generateClubDetailUrl(UniversityCode universityCode, Long clubId) {
        return "%s/%s/club/%d".formatted(baseUrl, universityCode.name().toLowerCase(), clubId);
    }
}
