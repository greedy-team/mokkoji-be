package com.greedy.mokkoji.notification;

import com.greedy.mokkoji.api.email.dto.ClubApplicationNotification;
import com.greedy.mokkoji.api.email.dto.ClubMasterApplicationNotification;
import com.greedy.mokkoji.api.email.service.DiscordNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
@DisplayName("디스코드 웹훅 알림 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class DiscordNotifierTest {

    private static final String CLUB_APPLICATION_WEBHOOK_URL = "https://discord.test/club-application";
    private static final String CLUB_MASTER_APPLICATION_WEBHOOK_URL = "https://discord.test/club-master-application";

    @InjectMocks
    DiscordNotifier discordNotifier;

    @Mock
    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(discordNotifier, "clubApplicationWebhookUrl", CLUB_APPLICATION_WEBHOOK_URL);
        ReflectionTestUtils.setField(discordNotifier, "clubMasterApplicationWebhookUrl", CLUB_MASTER_APPLICATION_WEBHOOK_URL);
    }

    @Test
    @DisplayName("동아리 생성 신청 알림은 학교/동아리/신청자 정보를 담아 전송한다.")
    void notifyClubApplicationCreated() {
        // given
        final ClubApplicationNotification notification = new ClubApplicationNotification(
                1L,
                "세종대학교",
                "그리디",
                "학술/교양",
                "중앙",
                "홍길동",
                "hong@test.com",
                "https://instagram.com/greedy",
                "알고리즘을 공부하는 동아리입니다."
        );

        // when
        discordNotifier.notifyClubApplicationCreated(notification);

        // then
        final String content = captureSentContent(CLUB_APPLICATION_WEBHOOK_URL);
        assertThat(content).contains(
                "동아리 생성 신청 알림",
                "세종대학교",
                "그리디",
                "학술/교양",
                "중앙",
                "홍길동",
                "hong@test.com",
                "https://instagram.com/greedy",
                "알고리즘을 공부하는 동아리입니다."
        );
    }

    @Test
    @DisplayName("동아리장 권한 신청 알림은 학교/동아리/신청자 정보를 담아 전송한다.")
    void notifyClubMasterApplicationCreated() {
        // given
        final ClubMasterApplicationNotification notification = new ClubMasterApplicationNotification(
                2L,
                "세종대학교",
                "그리디",
                "홍길동",
                "hong@test.com"
        );

        // when
        discordNotifier.notifyClubMasterApplicationCreated(notification);

        // then
        final String content = captureSentContent(CLUB_MASTER_APPLICATION_WEBHOOK_URL);
        assertThat(content).contains(
                "동아리장 권한 신청 알림",
                "세종대학교",
                "그리디",
                "홍길동",
                "hong@test.com"
        );
    }

    @Test
    @DisplayName("값이 비어 있는 항목은 '-'로 대체되어 전송된다.")
    void notifyClubApplicationCreatedWithEmptyValues() {
        // given
        final ClubApplicationNotification notification = new ClubApplicationNotification(
                1L,
                "세종대학교",
                "그리디",
                "학술/교양",
                "중앙",
                "홍길동",
                null,
                null,
                ""
        );

        // when
        discordNotifier.notifyClubApplicationCreated(notification);

        // then
        final String content = captureSentContent(CLUB_APPLICATION_WEBHOOK_URL);
        assertThat(content).contains(": -");
        assertThat(content).doesNotContain("null");
    }

    @Test
    @DisplayName("웹훅 URL이 비어 있으면 전송하지 않는다.")
    void doNotNotifyWhenWebhookUrlIsEmpty() {
        // given
        ReflectionTestUtils.setField(discordNotifier, "clubApplicationWebhookUrl", "");
        ReflectionTestUtils.setField(discordNotifier, "clubMasterApplicationWebhookUrl", "");

        // when
        discordNotifier.notifyClubApplicationCreated(new ClubApplicationNotification(
                1L, "세종대학교", "그리디", "학술/교양", "중앙", "홍길동", "hong@test.com", null, null
        ));
        discordNotifier.notifyClubMasterApplicationCreated(new ClubMasterApplicationNotification(
                2L, "세종대학교", "그리디", "홍길동", "hong@test.com"
        ));

        // then
        BDDMockito.verifyNoInteractions(restTemplate);
    }

    @SuppressWarnings("unchecked")
    private String captureSentContent(final String expectedWebhookUrl) {
        final ArgumentCaptor<HttpEntity<Map<String, String>>> captor = ArgumentCaptor.forClass((Class) HttpEntity.class);
        BDDMockito.verify(restTemplate).postForEntity(eq(expectedWebhookUrl), captor.capture(), eq(String.class));

        return captor.getValue().getBody().get("content");
    }
}
