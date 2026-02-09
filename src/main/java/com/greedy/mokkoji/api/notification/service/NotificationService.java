package com.greedy.mokkoji.api.notification.service;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.greedy.mokkoji.db.favorite.entity.QFavorite.favorite;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationChannel notificationChannel;
    private final FavoriteRepository favoriteRepository;

    @Async
    @Transactional(readOnly = true)
    public void sendNotification(final Club club, final Recruitment recruitment) {
        List<Favorite> favorites = favoriteRepository.findByClubIdWithFetchJoin(club.getId());

        List<String> userEmails = favorites.stream()
                .map(favorite -> favorite.getUser())
                .filter(user -> user.isEmailOn())
                .map(user -> user.getEmail())
                .filter(email -> email != null)
                .toList();

        if (userEmails.isEmpty()) {
            return;
        }

        notificationChannel.sendNotification(
                userEmails, club.getId(), club.getName(), recruitment.getRecruitStart(), recruitment.getRecruitEnd()
        );
    }
}
