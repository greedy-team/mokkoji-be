package com.greedy.mokkoji.api.notification.sse;

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

    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(final Long userId) {
        final SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

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
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected")
                    .reconnectTime(RECONNECTION_TIMEOUT));
        } catch (IOException e) {
            log.warn("SSE 초기 메시지 전송 실패 : userId={}", userId, e);
            emitter.complete();
            remove(userId, emitter);
        }

        return emitter;
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
            } catch (IOException e) {
                log.warn("SSE 전송 실패 : userId={}, event={}", userId, eventName, e);
                emitter.complete();
                remove(userId, emitter);
            }
        });
    }

    private void remove(final Long userId, final SseEmitter emitter) {
        final List<SseEmitter> userEmitters = emitters.get(userId);
        if (userEmitters == null) {
            return;
        }

        userEmitters.remove(emitter);
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
