package com.greedy.mokkoji.api.test.notification;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClubRecruitmentNotificationScheduler {
    private final RecruitmentRepository recruitmentRepository;
    private final NotificationService notificationService;

    public ClubRecruitmentNotificationScheduler(
            final RecruitmentRepository recruitmentRepository,
            final NotificationService notificationService
    ) {
        this.recruitmentRepository = recruitmentRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    private void sendDailyRecruitmentNotifications() {
        LocalDate today = LocalDate.now();

        List<Recruitment> recruitments = recruitmentRepository.findTodayRecruitStartDate(today);

        for (Recruitment recruitment : recruitments) {
            Club club = recruitment.getClub();
            notificationService.sendNotification(club, recruitment);
        }
    }
}
