package com.greedy.mokkoji.api.scheduler.service;

import com.greedy.mokkoji.api.notification.sse.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationHeartbeatScheduler {

    private static final long HEARTBEAT_INTERVAL = 30L * 1000;

    private final NotificationSseService notificationSseService;

    @Scheduled(fixedRate = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        notificationSseService.sendHeartbeat();
    }
}
