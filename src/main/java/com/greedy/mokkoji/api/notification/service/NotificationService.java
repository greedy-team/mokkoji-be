package com.greedy.mokkoji.api.notification.service;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final RecruitmentNotificationChannel recruitmentNotificationChannel;
    private final FavoriteRepository favoriteRepository;

    @Async
    @Transactional(readOnly = true)
    public void sendNotification(final Long clubId, final String clubName, final Recruitment recruitment) {
        List<Favorite> favorites = favoriteRepository.findByClubIdWithFetchJoin(clubId);

        List<String> userEmails = favorites.stream()
                .map(favorite -> favorite.getUser())
                .filter(user -> user.isEmailOn())
                .map(user -> user.getEmail())
                .filter(email -> email != null)
                .toList();

        if (userEmails.isEmpty()) {
            return;
        }

        recruitmentNotificationChannel.sendNotification(
                userEmails, clubId, clubName, recruitment.getRecruitStart(), recruitment.getRecruitEnd()
        );
    }
}
