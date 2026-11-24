package com.greedy.mokkoji.notification.sse;

import com.greedy.mokkoji.api.notification.sse.NotificationSseService;
import com.greedy.mokkoji.enums.user.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    @DisplayName("마지막 emitter 제거와 새 구독이 동시에 일어나도 새 emitter는 전송 대상에 남는다.")
    void keepNewEmitterWhenSubscribeRacesWithRemove() throws Exception {
        final Long userId = 1L;
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        int lost = 0;

        try {
            for (int i = 0; i < 20000; i++) {
                final SseEmitter oldEmitter = notificationSseService.subscribe(userId, UserRole.NORMAL);
                final CyclicBarrier barrier = new CyclicBarrier(2);

                final Future<?> removing = executor.submit(() -> {
                    barrier.await();
                    ReflectionTestUtils.invokeMethod(notificationSseService, "remove", userId, oldEmitter);
                    return null;
                });
                final Future<SseEmitter> subscribing = executor.submit(() -> {
                    barrier.await();
                    return notificationSseService.subscribe(userId, UserRole.NORMAL);
                });

                removing.get();
                final SseEmitter newEmitter = subscribing.get();

                final List<SseEmitter> userEmitters = emitters().get(userId);
                if (userEmitters == null || !userEmitters.contains(newEmitter)) {
                    lost++;
                }
                emitters().clear();
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(lost).isZero();
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

    @Test
    @DisplayName("구독 시 connect 이벤트에 현재 role이 함께 전달된다.")
    void sendRoleWithConnectEvent() throws IOException {
        // given
        final SseEmitter emitter = BDDMockito.mock(SseEmitter.class);

        // when
        notificationSseService.sendConnect(emitter, UserRole.CLUB_MASTER);

        // then
        final ArgumentCaptor<SseEmitter.SseEventBuilder> captor =
                ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        BDDMockito.verify(emitter).send(captor.capture());

        final String payload = captor.getValue().build().stream()
                .map(dataWithMediaType -> String.valueOf(dataWithMediaType.getData()))
                .reduce("", String::concat);

        assertThat(payload).contains("connect");
        assertThat(payload).contains("CLUB_MASTER");
    }

    @Test
    @DisplayName("role을 알 수 없으면 connect 이벤트에 role 없이 connected만 전달된다.")
    void sendConnectWithoutRoleWhenRoleIsUnknown() throws IOException {
        // given
        final SseEmitter emitter = BDDMockito.mock(SseEmitter.class);

        // when
        notificationSseService.sendConnect(emitter, null);

        // then
        final ArgumentCaptor<SseEmitter.SseEventBuilder> captor =
                ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        BDDMockito.verify(emitter).send(captor.capture());

        final String payload = captor.getValue().build().stream()
                .map(dataWithMediaType -> String.valueOf(dataWithMediaType.getData()))
                .reduce("", String::concat);

        assertThat(payload).contains("connected");
        assertThat(payload).doesNotContain("CLUB_MASTER");
        assertThat(payload).doesNotContain("NORMAL");
    }
}
