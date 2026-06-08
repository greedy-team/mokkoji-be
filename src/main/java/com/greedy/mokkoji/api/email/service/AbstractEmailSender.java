package com.greedy.mokkoji.api.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
public abstract class AbstractEmailSender {

    protected static final String SENDER_NAME = "모꼬지(mokkoji)";
    protected static final String OFFICIAL_EMAIL = "noreply@mokkoji.com";

    protected final JavaMailSender mailSender;
    protected final DiscordNotifier discordNotifier;

    @Value("${spring.mail.username}")
    protected String senderMail;
    @Value("${mokkoji.base-url}")
    protected String baseUrl;
    @Value("${mokkoji.mail.banner-url}")
    protected String mailBannerUrl;

    protected AbstractEmailSender(JavaMailSender mailSender, DiscordNotifier discordNotifier) {
        this.mailSender = mailSender;
        this.discordNotifier = discordNotifier;
    }

    protected void sendEmailInternal(
            List<String> receiverMails,
            String subject,
            String htmlContent,
            Runnable messagingErrorHandler,
            Runnable mailErrorHandler,
            Runnable unexpectedErrorHandler
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(senderMail, SENDER_NAME);
            helper.setTo(OFFICIAL_EMAIL);
            helper.setBcc(receiverMails.toArray(new String[0]));
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("[MAIL GENERATING ERROR] message={}", e.getMessage(), e);
            messagingErrorHandler.run();
        } catch (MailException e) {
            log.error("[MAIL SEND FAILED] message={}", e.getMessage(), e);
            mailErrorHandler.run();
        } catch (Exception e) {
            log.error("[EMAIL UNEXPECTED ERROR]", e);
            unexpectedErrorHandler.run();
        }
    }
}

