package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailService {

    // emailExecutor corePoolSize와 동일 — 항상 모든 스레드를 활용하기 위해 청크 수를 풀 크기로 고정
    private static final int EMAIL_POOL_SIZE = 5;

    private final RecruitmentNotificationChannel recruitmentNotificationChannel;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public void sendBatchRecruitmentNotifications(final List<Recruitment> recruitments) {
        List<Long> clubIds = recruitments.stream()
                .map(r -> r.getClub().getId())
                .toList();

        Map<Long, List<String>> emailsByClubId = favoriteRepository.findByClubIdInWithFetchJoin(clubIds).stream()
                .filter(f -> f.getUser() != null && f.getUser().isEmailOn())
                .filter(f -> f.getUser().getEmail() != null && !f.getUser().getEmail().isBlank())
                .collect(Collectors.groupingBy(
                        f -> f.getClub().getId(),
                        Collectors.mapping(f -> f.getUser().getEmail(), Collectors.toList())
                ));

        List<RecruitmentMailPayload> payloads = recruitments.stream()
                .filter(r -> emailsByClubId.containsKey(r.getClub().getId()))
                .map(r -> new RecruitmentMailPayload(
                        r.getClub().getId(),
                        r.getClub().getName(),
                        r.getClub().getUniversity().getCode(),
                        emailsByClubId.get(r.getClub().getId()),
                        r.getRecruitStart(),
                        r.getRecruitEnd()
                ))
                .toList();

        int chunkCount = Math.min(EMAIL_POOL_SIZE, payloads.size());
        for (int i = 0; i < chunkCount; i++) {
            int from = i * payloads.size() / chunkCount;
            int to = (i + 1) * payloads.size() / chunkCount;
            recruitmentNotificationChannel.sendBatchNotification(payloads.subList(from, to), i + 1);
        }
    }
}
