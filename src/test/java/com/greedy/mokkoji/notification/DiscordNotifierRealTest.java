package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.dto.ClubApplicationNotification;
import com.greedy.mokkoji.api.email.dto.ClubMasterApplicationNotification;
import com.greedy.mokkoji.api.email.dto.FeedbackNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

@DisplayName("실제 디스코드 웹훅 발송 테스트")
@Disabled("디스코드 웹훅 테스트 시 해제를 하고 사용")
public class DiscordNotifierRealTest {

    private final DiscordNotifier discordNotifier = new DiscordNotifier(new RestTemplate());

    @BeforeEach
    void setUp() {
        final YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));
        final Properties properties = yaml.getObject();

        ReflectionTestUtils.setField(discordNotifier, "clubApplicationWebhookUrl",
                properties.getProperty("discord.webhook.club-application.url"));
        ReflectionTestUtils.setField(discordNotifier, "clubApplicationEnabled", true);
        ReflectionTestUtils.setField(discordNotifier, "clubMasterApplicationWebhookUrl",
                properties.getProperty("discord.webhook.club-master-application.url"));
        ReflectionTestUtils.setField(discordNotifier, "clubMasterApplicationEnabled", true);
        ReflectionTestUtils.setField(discordNotifier, "feedbackWebhookUrl",
                properties.getProperty("discord.webhook.feedback.url"));
        ReflectionTestUtils.setField(discordNotifier, "feedbackEnabled", true);
    }

    @Test
    @DisplayName("동아리 생성 신청 알림 실제 발송 확인")
    void sendClubApplicationNotification() {
        discordNotifier.notifyClubApplicationCreated(new ClubApplicationNotification(
                1L,
                "세종대학교",
                "그리디",
                "학술/교양",
                "중앙",
                "홍길동",
                "hong@test.com",
                "https://instagram.com/greedy",
                "알고리즘을 공부하는 동아리입니다."
        ));
    }

    @Test
    @DisplayName("동아리장 권한 신청 알림 실제 발송 확인")
    void sendClubMasterApplicationNotification() {
        discordNotifier.notifyClubMasterApplicationCreated(new ClubMasterApplicationNotification(
                2L,
                "세종대학교",
                "그리디",
                "홍길동",
                "hong@test.com"
        ));
    }

    @Test
    @DisplayName("피드백 등록 알림 실제 발송 확인")
    void sendFeedbackNotification() {
        discordNotifier.notifyFeedbackCreated(new FeedbackNotification(
                999L,
                4,
                "웹훅 이전 테스트입니다."
        ));
    }
}
