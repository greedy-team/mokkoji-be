package com.greedy.mokkoji.api.mail.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.greedy.mokkoji.enums.message.FailMessage.*;

@Slf4j
@Component
public class ClubMasterTransferEmailChannel extends AbstractEmailSender {

    private static final String SUBJECT_SUFFIX = " 동아리장 권한 위임 요청";

    public ClubMasterTransferEmailChannel(JavaMailSender mailSender, DiscordNotifier discordNotifier) {
        super(mailSender, discordNotifier);
    }

    @Async
    public void sendClubMasterTransferEmail(
            final String nextClubMasterEmail,
            final String clubName,
            final String clubMasterTransferLink
    ) {
        String subject = clubName + SUBJECT_SUFFIX;
        String htmlContent = generateHtmlText(clubName, clubMasterTransferLink);

        sendEmailInternal(
                List.of(nextClubMasterEmail),
                subject,
                htmlContent,
                () -> discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR_SMTP_MAIL.getMessage()),
                () -> discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR_SMTP.getMessage()),
                () -> discordNotifier.notifyClubMasterTransferEmailFailure(clubName, nextClubMasterEmail, INTERNAL_SERVER_ERROR.getMessage())
        );
    }

    private String generateHtmlText(String clubName, String clubMasterTransferLink) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: Arial, sans-serif;">
                    <img src="%s" alt="모꼬지 배너" style="width: 100%%; max-width: 600px; display: block; margin: 0 0 20px 0;" />
                    <div style="margin: 40px">
                        <h3>📣 %s 동아리장 권한 위임 요청</h3>
                        <p>안녕하세요! <strong>모꼬지</strong>입니다:)</p>
                        <p><strong>%s</strong>의 동아리장 권한 위임 요청이 도착했습니다.</p>
                        <p>아래 버튼을 클릭하여 권한을 수락해주세요.</p>
                        <p><strong>⚠️ 링크는 10분 후 만료됩니다.</strong></p>
                        <p>모꼬지 드림.</p>
                        <a href="%s/%s" style="display: inline-block; padding: 10px 15px; font-size: 14px; color: #000000; background-color: #4AF38A; text-decoration: none; border-radius: 40px; font-weight: 500;">권한 수락하러 가기</a>
                    </div>
                </body>
                </html>
                """.formatted(mailBannerUrl, clubName, clubName, baseUrl, clubMasterTransferLink);
    }
}
