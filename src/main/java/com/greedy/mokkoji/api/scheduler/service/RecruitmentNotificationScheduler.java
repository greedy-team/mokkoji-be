package com.greedy.mokkoji.api.scheduler.service;

import com.greedy.mokkoji.api.notification.service.NotificationService;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentNotificationScheduler {

    private final RecruitmentRepository recruitmentRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${schedules.cron.reward.publish}", zone = "${schedules.cron.reward.zone}")
    @Transactional
    public void sendDailyRecruitmentNotifications() {
        final LocalDate today = LocalDate.now();
        final LocalDate threeDaysLater = today.plusDays(3);

        List<Recruitment> recruitments = recruitmentRepository.findAllByRecruitStartToday(today);

        recruitments.addAll(recruitmentRepository.findAllByRecruitEndToday(today));
        recruitments.addAll(recruitmentRepository.findAllByRecruitEndInThreeDays(threeDaysLater));

        // 1. Club을 키로, Recruitment를 값으로 하는 Map을 생성 (중복 제거 및 최신 선택)
        Map<Club, Recruitment> latestRecruitmentsByClub = new HashMap<>();

        for (Recruitment recruitment : recruitments) {
            Club club = recruitment.getClub();

            // 이미 해당 Club에 대한 Recruitment가 Map에 있다면
            if (latestRecruitmentsByClub.containsKey(club)) {
                Recruitment existingRecruitment = latestRecruitmentsByClub.get(club);

                // 현재 것이 더 최신이면 Map의 값을 업데이트
                if (recruitment.getCreatedAt().isAfter(existingRecruitment.getCreatedAt())) {
                    latestRecruitmentsByClub.put(club, recruitment);
                }
            } else {
                // Map에 없으면 새로운 항목으로 추가
                latestRecruitmentsByClub.put(club, recruitment);
            }
        }

        // 2. Map의 값(가장 최신 Recruitment들)만 추출하여 List로 다시 구성
        List<Recruitment> uniqueAndLatestRecruitments = new ArrayList<>(latestRecruitmentsByClub.values());

        // 3. 원래의 recruitments 변수에 새 List를 대입하여 forEach문의 입력으로 사용
        recruitments = uniqueAndLatestRecruitments;

        recruitments.forEach(recruitment -> {
            Club club = recruitment.getClub();
            notificationService.sendNotification(club, recruitment);
        });

        recruitments.stream()
                .filter(recruitment -> !recruitment.isAlwaysRecruiting())
                .forEach(recruitment -> {
                    Club club = recruitment.getClub();
                    notificationService.sendNotification(club, recruitment);
                });
    }
}

