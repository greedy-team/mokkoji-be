package com.greedy.mokkoji.api.email.service;

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
    @Value("${discord.webhook.club-master-transfer-mail-fail.url}")
    private String clubMasterTransferMailFailWebhookUrl;
    @Value("${discord.webhook.club-master-transfer-mail-fail.enabled}")
    private boolean clubMasterTransferMailEnabled;


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
    public void notifyClubMasterTransferEmailFailure(String clubName, String nextClubMasterEmail, String errorMessage) {
        if (!clubMasterTransferMailEnabled ||
                clubMasterTransferMailFailWebhookUrl == null ||
                clubMasterTransferMailFailWebhookUrl.isEmpty()) {
            return;
        }

        String timestamp = LocalDateTime.now().format(FORMATTER);

        String content = String.format(
                "🚨 **동아리장 권한 위임 이메일 발송 실패 알림**\n" +
                        "```text\n" +
                        "동아리명        : %s\n" +
                        "수신자 이메일  : %s\n" +
                        "에러 내용      : %s\n" +
                        "발생 시간      : %s\n" +
                        "```",
                clubName, nextClubMasterEmail, errorMessage, timestamp
        );
        sendToDiscord(content, clubMasterTransferMailFailWebhookUrl);
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
