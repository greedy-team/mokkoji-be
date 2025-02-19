package com.greedy.mokkoji.api.test.notification.service;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentNotificationScheduler {
    private final RecruitmentRepository recruitmentRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${schedules.cron.reward.publish}", zone = "${schedules.cron.reward.zone}")
    @Transactional
    public void sendDailyRecruitmentNotifications() {
        log.info("[SCHEDULER START]");
        final LocalDate currentDate = LocalDate.now();

        List<Recruitment> recruitments = recruitmentRepository.findTodayRecruitStartDate(currentDate);

        for (Recruitment recruitment : recruitments) {
            Club club = recruitment.getClub();
            notificationService.sendNotification(club, recruitment);
        }

        log.info("[SCHEDULER END]");
    }
}
