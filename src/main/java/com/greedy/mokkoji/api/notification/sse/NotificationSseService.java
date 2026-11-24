package com.greedy.mokkoji.api.notification.sse;

import com.greedy.mokkoji.api.notification.dto.ConnectPayload;
import com.greedy.mokkoji.enums.user.UserRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class NotificationSseService {

    private static final long DEFAULT_TIMEOUT = 60L * 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 3000L;
    private static final String HEARTBEAT_COMMENT = "heartbeat";
    private static final String EVENT_CONNECT = "connect";

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(final Long userId, final UserRole role) {
        final SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        add(userId, emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });
        emitter.onError(e -> {
            emitter.complete();
            remove(userId, emitter);
        });

        try {
            sendConnect(emitter, role);
        } catch (IOException e) {
            log.warn("SSE 초기 메시지 전송 실패 : userId={}", userId, e);
            emitter.complete();
            remove(userId, emitter);
        }

        return emitter;
    }

    public void sendConnect(final SseEmitter emitter, final UserRole role) throws IOException {
        emitter.send(SseEmitter.event()
                .name(EVENT_CONNECT)
                .data(ConnectPayload.of(role))
                .reconnectTime(RECONNECTION_TIMEOUT));
    }

    public void send(final Long userId, final String eventName, final Object data) {
        final List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null || userEmitters.isEmpty()) {
            return;
        }

        userEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data)
                        .reconnectTime(RECONNECTION_TIMEOUT));
            } catch (IOException | IllegalStateException e) {
                log.warn("SSE 전송 실패 : userId={}, event={}", userId, eventName, e);
                emitter.complete();
                remove(userId, emitter);
            }
        });
    }

    public void sendHeartbeat() {
        emitters.forEach((userId, userEmitters) -> userEmitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().comment(HEARTBEAT_COMMENT));
            } catch (IOException | IllegalStateException e) {
                log.debug("SSE heartbeat 전송 실패 : userId={}", userId, e);
                emitter.complete();
                remove(userId, emitter);
            }
        }));
    }

    private void add(final Long userId, final SseEmitter emitter) {
        emitters.compute(userId, (key, userEmitters) -> {
            if (userEmitters == null) {
                return new CopyOnWriteArrayList<>(List.of(emitter));
            }
            userEmitters.add(emitter);
            return userEmitters;
        });
    }

    private void remove(final Long userId, final SseEmitter emitter) {
        emitters.computeIfPresent(userId, (key, userEmitters) -> {
            userEmitters.remove(emitter);
            return userEmitters.isEmpty() ? null : userEmitters;
        });
    }
}
