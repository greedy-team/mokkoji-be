package com.greedy.mokkoji.api.notification.service;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationChannel notificationChannel;
    private final FavoriteRepository favoriteRepository;

    @Transactional
    public void sendNotification(final Club club, final Recruitment recruitment) {
        List<Favorite> favorites = favoriteRepository.findByClubIdWithFetchJoin(club.getId());

        List<String> userEmail = favorites.stream()
                .map(favorite -> favorite.getUser().getEmail())
                .filter(email -> email != null)
                .toList();

        if (userEmail.isEmpty()) {
            return;
        }

        notificationChannel.sendNotification(
                userEmail, club.getId(), club.getName(), recruitment.getRecruitStart(), recruitment.getRecruitEnd()
        );
    }

}
