package com.greedy.mokkoji.notification.sse;

import com.greedy.mokkoji.api.notification.sse.NotificationSseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("알림 SSE 서비스 테스트")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NotificationSseServiceTest {

    NotificationSseService notificationSseService = new NotificationSseService();

    @SuppressWarnings("unchecked")
    private Map<Long, List<SseEmitter>> emitters() {
        return (Map<Long, List<SseEmitter>>) ReflectionTestUtils.getField(notificationSseService, "emitters");
    }

    private SseEmitter registerEmitter(final Long userId) {
        final SseEmitter emitter = BDDMockito.mock(SseEmitter.class);
        emitters().computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        return emitter;
    }

    @Test
    @DisplayName("이미 종료된 emitter가 섞여 있어도 같은 사용자의 나머지 emitter에는 알림을 전송한다.")
    void keepSendingNotificationWhenOneEmitterAlreadyCompleted() throws IOException {
        final SseEmitter completedEmitter = registerEmitter(1L);
        final SseEmitter aliveEmitter = registerEmitter(1L);
        BDDMockito.willThrow(new IllegalStateException()).given(completedEmitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationSseService.send(1L, "notification", "data");

        BDDMockito.verify(aliveEmitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(emitters().get(1L)).containsExactly(aliveEmitter);
    }

    @Test
    @DisplayName("알림 전송에 실패한 emitter는 제거한다.")
    void removeEmitterWhenNotificationFails() throws IOException {
        final SseEmitter brokenEmitter = registerEmitter(1L);
        BDDMockito.willThrow(new IOException()).given(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationSseService.send(1L, "notification", "data");

        BDDMockito.verify(brokenEmitter).complete();
        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    @DisplayName("연결된 모든 emitter에 heartbeat를 전송한다.")
    void sendHeartbeatToAllEmitters() throws IOException {
        final SseEmitter firstEmitter = registerEmitter(1L);
        final SseEmitter secondEmitter = registerEmitter(2L);

        notificationSseService.sendHeartbeat();

        BDDMockito.verify(firstEmitter).send(any(SseEmitter.SseEventBuilder.class));
        BDDMockito.verify(secondEmitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(emitters()).containsKeys(1L, 2L);
    }

    @Test
    @DisplayName("heartbeat 전송에 실패한 emitter는 제거한다.")
    void removeEmitterWhenHeartbeatFails() throws IOException {
        final SseEmitter brokenEmitter = registerEmitter(1L);
        BDDMockito.willThrow(new IOException()).given(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationSseService.sendHeartbeat();

        BDDMockito.verify(brokenEmitter).complete();
        assertThat(emitters()).doesNotContainKey(1L);
    }

    @Test
    @DisplayName("일부 emitter가 실패해도 나머지에는 heartbeat를 전송한다.")
    void keepSendingHeartbeatWhenOneEmitterFails() throws IOException {
        final SseEmitter brokenEmitter = registerEmitter(1L);
        final SseEmitter aliveEmitter = registerEmitter(2L);
        BDDMockito.willThrow(new IllegalStateException()).given(brokenEmitter).send(any(SseEmitter.SseEventBuilder.class));

        notificationSseService.sendHeartbeat();

        BDDMockito.verify(aliveEmitter).send(any(SseEmitter.SseEventBuilder.class));
        assertThat(emitters()).containsOnlyKeys(2L);
    }
}
