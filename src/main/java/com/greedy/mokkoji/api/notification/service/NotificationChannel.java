package com.greedy.mokkoji.api.notification.service;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationChannel {
    void sendNotification(
            List<String> receiverMails,
            Long clubId,
            String clubName,
            LocalDateTime recruitStartTime,
            LocalDateTime recruitEndTime
    );
}
