package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.api.email.dto.ClubApplicationNotification;
import com.greedy.mokkoji.api.email.dto.ClubMasterApplicationNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordNotifier {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RestTemplate restTemplate;
    @Value("${discord.webhook.recruitment-notification-mail-fail.url}")
    private String recruitmentNotificationMailFailWebhookUrl;
    @Value("${discord.webhook.recruitment-notification-mail-fail.enabled}")
    private boolean recruitmentNotificationMailEnabled;
    @Value("${discord.webhook.club-application.url}")
    private String clubApplicationWebhookUrl;
    @Value("${discord.webhook.club-master-application.url}")
    private String clubMasterApplicationWebhookUrl;

    @Async("discordExecutor")
    public void notifyRecruitmentNotificationEmailFailure(Long clubId, String clubName, int receiverCount, String errorMessage) {
        if (!recruitmentNotificationMailEnabled ||
                recruitmentNotificationMailFailWebhookUrl == null
                || recruitmentNotificationMailFailWebhookUrl.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);

        String content = String.format(
                "🚨 **이메일 발송 실패 알림**\n" +
                        "```text\n" +
                        "동아리 ID   : %d\n" +
                        "동아리명    : %s\n" +
                        "수신자 수  : %d명\n" +
                        "에러 내용  : %s\n" +
                        "발생 시간  : %s\n" +
                        "```",
                clubId, clubName, receiverCount, errorMessage, timestamp
        );
        sendToDiscord(content, recruitmentNotificationMailFailWebhookUrl);
    }

    @Async("discordExecutor")
    public void notifyClubApplicationCreated(final ClubApplicationNotification clubApplicationNotification) {
        if (clubApplicationWebhookUrl == null || clubApplicationWebhookUrl.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);

        String content = String.format(
                "📋 **동아리 생성 신청 알림**\n" +
                        "```text\n" +
                        "신청 ID     : %d\n" +
                        "학교        : %s\n" +
                        "동아리명    : %s\n" +
                        "카테고리   : %s\n" +
                        "소속       : %s\n" +
                        "신청자     : %s\n" +
                        "이메일     : %s\n" +
                        "인스타그램 : %s\n" +
                        "소개       : %s\n" +
                        "신청 시간  : %s\n" +
                        "```",
                clubApplicationNotification.applicationId(),
                clubApplicationNotification.universityName(),
                clubApplicationNotification.clubName(),
                clubApplicationNotification.clubCategory(),
                clubApplicationNotification.clubAffiliation(),
                clubApplicationNotification.applicantName(),
                orDash(clubApplicationNotification.applicantEmail()),
                orDash(clubApplicationNotification.instagram()),
                orDash(clubApplicationNotification.description()),
                timestamp
        );
        sendToDiscord(content, clubApplicationWebhookUrl);
    }

    @Async("discordExecutor")
    public void notifyClubMasterApplicationCreated(final ClubMasterApplicationNotification clubMasterApplicationNotification) {
        if (clubMasterApplicationWebhookUrl == null || clubMasterApplicationWebhookUrl.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);

        String content = String.format(
                "👑 **동아리장 권한 신청 알림**\n" +
                        "```text\n" +
                        "신청 ID    : %d\n" +
                        "학교       : %s\n" +
                        "동아리명   : %s\n" +
                        "신청자     : %s\n" +
                        "이메일     : %s\n" +
                        "신청 시간  : %s\n" +
                        "```",
                clubMasterApplicationNotification.applicationId(),
                clubMasterApplicationNotification.universityName(),
                clubMasterApplicationNotification.clubName(),
                clubMasterApplicationNotification.applicantName(),
                orDash(clubMasterApplicationNotification.applicantEmail()),
                timestamp
        );
        sendToDiscord(content, clubMasterApplicationWebhookUrl);
    }

    private String orDash(final String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }
        return value;
    }

    private void sendToDiscord(String content, String webhookUrl) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(webhookUrl, request, String.class);
        } catch (Exception e) {
            log.error("[DISCORD WEBHOOK FAILED] message={}", e.getMessage());
        }
    }
}
