package com.greedy.mokkoji.api.test.notification;

import com.greedy.mokkoji.api.test.notification.service.RecruitmentNotificationScheduler;
import com.greedy.mokkoji.api.test.notification.service.NotificationService;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/test")
@Slf4j
public class NotiTestController {
    private final RecruitmentNotificationScheduler clubRecruitmentNotificationScheduler;

    public NotiTestController(RecruitmentNotificationScheduler clubRecruitmentNotificationScheduler) {
        this.clubRecruitmentNotificationScheduler = clubRecruitmentNotificationScheduler;
    }

    @PostMapping("/send")
    public void sendNoti() {
        clubRecruitmentNotificationScheduler.sendDailyRecruitmentNotifications();
    }
}
