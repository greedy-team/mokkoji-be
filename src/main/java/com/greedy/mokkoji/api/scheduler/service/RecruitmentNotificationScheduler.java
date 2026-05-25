package com.greedy.mokkoji.api.scheduler.service;

import com.greedy.mokkoji.api.mail.service.EmailService;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentNotificationScheduler {

    private final RecruitmentRepository recruitmentRepository;
    private final EmailService emailService;

    @Scheduled(cron = "${schedules.cron.reward.publish}", zone = "${schedules.cron.reward.zone}")
    @Transactional
    public void sendDailyRecruitmentNotifications() {
        final LocalDate today = LocalDate.now();
        final LocalDate threeDaysLater = today.plusDays(3);

        List<Recruitment> recruitments = new ArrayList<>(recruitmentRepository.findAllByRecruitStartToday(today));

        recruitments.addAll(recruitmentRepository.findAllByRecruitEndToday(today));
        recruitments.addAll(recruitmentRepository.findAllByRecruitEndInThreeDays(threeDaysLater));

        List<Recruitment> uniqueAndLatestRecruitments = recruitments.stream()
                .filter(recruitment -> !recruitment.isAlwaysRecruiting())
                .collect(Collectors.toMap(
                        recruitment -> recruitment.getClub().getId(),
                        recruitment -> recruitment,
                        (recruitment1, recruitment2) ->
                                recruitment1.getCreatedAt().isAfter(recruitment2.getCreatedAt())
                                        ? recruitment1
                                        : recruitment2
                ))
                .values()
                .stream()
                .toList();

        uniqueAndLatestRecruitments.forEach(recruitment -> {
            Club club = recruitment.getClub();
            try {
                emailService.sendNotification(club.getId(), club.getName(), recruitment);
            } catch (Exception e) {
                log.error("[RECRUITMENT NOTI SUBMIT FAILED] clubId={} recruitmentId={} msg={}",
                        club.getId(), recruitment.getId(), e.getMessage(), e);
            }
        });
    }
}

