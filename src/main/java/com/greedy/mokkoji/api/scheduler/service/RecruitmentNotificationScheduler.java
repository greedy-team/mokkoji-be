package com.greedy.mokkoji.api.scheduler.service;

import com.greedy.mokkoji.api.email.service.EmailService;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        final LocalDateTime startOfToday = today.atStartOfDay();
        final LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();

        final LocalDate threeDaysLater = today.plusDays(3);
        final LocalDateTime startOfThreeDaysLater = threeDaysLater.atStartOfDay();
        final LocalDateTime startOfFourDaysLater = threeDaysLater.plusDays(1).atStartOfDay();

        List<Recruitment> recruitments = new ArrayList<>(recruitmentRepository.findAllByRecruitStartToday(startOfToday, startOfTomorrow));
        recruitments.addAll(recruitmentRepository.findAllByRecruitEndToday(startOfToday, startOfTomorrow));
        recruitments.addAll(recruitmentRepository.findAllByRecruitEndInThreeDays(startOfThreeDaysLater, startOfFourDaysLater));

        List<Recruitment> uniqueAndLatestRecruitments = recruitments.stream()
                .filter(recruitment -> !recruitment.isAlwaysRecruiting())
                .collect(Collectors.toMap(
                        recruitment -> recruitment.getClub().getId(),
                        recruitment -> recruitment,
                        (r1, r2) -> r1.getCreatedAt().isAfter(r2.getCreatedAt()) ? r1 : r2
                ))
                .values()
                .stream()
                .toList();

        log.info("[RECRUITMENT NOTI] target={}", uniqueAndLatestRecruitments.size());

        try {
            emailService.sendBatchRecruitmentNotifications(uniqueAndLatestRecruitments);
        } catch (Exception e) {
            log.error("[RECRUITMENT NOTI SUBMIT FAILED] msg={}", e.getMessage(), e);
        }
    }
}
