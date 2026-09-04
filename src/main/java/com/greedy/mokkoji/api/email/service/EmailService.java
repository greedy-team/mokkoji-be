package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    // emailExecutor corePoolSize와 동일 — 항상 모든 스레드를 활용하기 위해 청크 수를 풀 크기로 고정
    private static final int EMAIL_POOL_SIZE = 5;

    private final RecruitmentNotificationChannel recruitmentNotificationChannel;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public void sendBatchRecruitmentNotifications(final List<Recruitment> recruitments) {
        List<RecruitmentMailPayload> payloads = new ArrayList<>();

        for (Recruitment recruitment : recruitments) {
            Long clubId = recruitment.getClub().getId();
            String clubName = recruitment.getClub().getName();

            List<String> userEmails = favoriteRepository.findByClubIdWithFetchJoin(clubId).stream()
                    .map(Favorite::getUser)
                    .filter(user -> user != null && user.isEmailOn())
                    .map(user -> user.getEmail())
                    .filter(email -> email != null && !email.isBlank())
                    .toList();

            if (!userEmails.isEmpty()) {
                var universityCode = recruitment.getClub().getUniversity().getCode();
                payloads.add(new RecruitmentMailPayload(
                        clubId, clubName, universityCode, userEmails,
                        recruitment.getRecruitStart(), recruitment.getRecruitEnd()
                ));
            }
        }

        int chunkCount = Math.min(EMAIL_POOL_SIZE, payloads.size());
        for (int i = 0; i < chunkCount; i++) {
            int from = i * payloads.size() / chunkCount;
            int to = (i + 1) * payloads.size() / chunkCount;
            recruitmentNotificationChannel.sendBatchNotification(payloads.subList(from, to));
        }
    }
}
