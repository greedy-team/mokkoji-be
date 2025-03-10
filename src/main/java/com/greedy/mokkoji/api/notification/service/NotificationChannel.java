package com.greedy.mokkoji.api.notification.service;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationChannel {
    void sendNotification(
            List<String> receiverMails,
            String clubName,
            LocalDateTime recruitStartTime,
            LocalDateTime recruitEndTime
    );
}
