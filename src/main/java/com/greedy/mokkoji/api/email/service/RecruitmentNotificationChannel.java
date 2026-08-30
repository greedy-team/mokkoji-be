package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.enums.university.UniversityCode;

import java.time.LocalDateTime;
import java.util.List;

public interface RecruitmentNotificationChannel {
    void sendNotification(
            List<String> receiverMails,
            Long clubId,
            String clubName,
            UniversityCode universityCode,
            LocalDateTime recruitStartTime,
            LocalDateTime recruitEndTime
    );
}
