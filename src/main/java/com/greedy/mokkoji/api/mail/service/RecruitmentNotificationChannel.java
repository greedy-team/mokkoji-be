package com.greedy.mokkoji.api.mail.service;

import java.time.LocalDateTime;
import java.util.List;

public interface RecruitmentNotificationChannel {
    void sendNotification(
            List<String> receiverMails,
            Long clubId,
            String clubName,
            LocalDateTime recruitStartTime,
            LocalDateTime recruitEndTime
    );
}
