package com.greedy.mokkoji.api.test.notification;

import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationChannel notificationChannel;
    private final FavoriteRepository favoriteRepository;

    public NotificationService(final NotificationChannel notificationChannel, final FavoriteRepository favoriteRepository) {
        this.notificationChannel = notificationChannel;
        this.favoriteRepository = favoriteRepository;
    }

    public void sendNotification(Club club, Recruitment recruitment) {
        List<Favorite> favorites = favoriteRepository.findByClubIdWithFetchJoin(club.getId());

        List<String> userEmail = favorites.stream().map(f -> f.getUser().getEmail()).toList();

        notificationChannel.sendNotification(userEmail, club.getName(), recruitment.getRecruitStart(), recruitment.getRecruitEnd());
    }
}
