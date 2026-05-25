package com.greedy.mokkoji.api.mail.service;

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

import static com.greedy.mokkoji.enums.message.FailMessage.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClubMasterTransferEmailChannel {
    private static final String SUBJECT = " 동아리장 권한 위임 요청";
    private static final String SENDER_NAME = "모꼬지(mokkoji)";
    private static final String OFFICIAL_EMAIL = "noreply@mokkoji.com";
    private final JavaMailSender mailSender;
    private final DiscordNotifier discordNotifier;

    @Value("${spring.mail.username}")
    private String senderMail;
    @Value("${mokkoji.base-url}")
    private String baseUrl;
    @Value("${mokkoji.mail.banner-url}")
    private String mailBannerUrl;

    public void sendClubMasterTransferEmail(
            final String nextClubMasterEmail,
            final String clubName,
            final String clubMasterTransferLink
    ) {
        try {
            final MimeMessage mimeMessage = mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            helper.setFrom(senderMail, SENDER_NAME);
            helper.setTo(OFFICIAL_EMAIL);
            helper.setBcc(nextClubMasterEmail);
            helper.setSubject(clubName + SUBJECT);

            final String text = generateHtmlText(clubName, clubMasterTransferLink);
            helper.setText(text, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("[MAIL GENERATING ERROR] clubName={} nextClubMasterEmail={} message={}",
                    clubName,
                    nextClubMasterEmail,
                    e.getMessage());
            discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR_SMTP_MAIL.getMessage());
        } catch (MailException e) {
            log.error("[MAIL SEND FAILED] clubName={} nextClubMasterEmail={} message={}",
                    clubName,
                    nextClubMasterEmail,
                    e.getMessage());
            discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR_SMTP.getMessage());
        } catch (Exception e) {
            log.error("[EMAIL UNEXPECTED ERROR] clubName={} nextClubMasterEmail={}",
                    clubName,
                    nextClubMasterEmail,
                    e);
            discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private String generateHtmlText(
            final String clubName,
            final String clubMasterTransferLink
    ) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>동아리장 권한 위임 요청</title>
                </head>
                <body style="font-family: Arial, sans-serif;">
                
                    <!-- 배너 이미지 -->
                    <img src="%s"
                         alt="모꼬지 배너"
                         style="width: 100%%; display: block; margin: 0 0 20px 0; max-width: 600px;" />
                    <div style="margin: 40px">
                    <h3>📣 %s 동아리장 권한 위임 요청</h3>
                        <p style="margin: 0 0 8px 0;">안녕하세요! <strong>모꼬지</strong>입니다:)</p>
                        <p style="margin: 0 0 30px 0;"><strong>%s</strong>의 동아리장 권한 위임 요청이 도착했습니다.</p>
                        <p style="margin: 0 0 8px 0;">아래 버튼을 클릭하여 권한을 수락해주세요.</p>
                        <p style="margin: 0 0 30px 0;"><strong>⚠️ 링크는 10분 후 만료됩니다.</strong></p>
                        <p style="margin: 0 0 30px 0;">모꼬지 드림.</p>
                
                        <a href="%s/%s"
                           style="display: inline-block; padding: 10px 15px;
                                  font-size: 14px; color: #000000; background-color: #4AF38A;
                                  text-decoration: none; border-radius: 40px; font-weight: 500;">
                           권한 수락하러 가기
                        </a>
                    </div>
                </body>
                </html>
                """.formatted(
                mailBannerUrl,
                clubName,
                clubName,
                baseUrl,
                clubMasterTransferLink
        );

    }
}

