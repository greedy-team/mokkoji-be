package com.greedy.mokkoji.api.email.service;

import java.util.List;

public interface RecruitmentNotificationChannel {
    void sendBatchNotification(List<RecruitmentMailPayload> payloads);
}
