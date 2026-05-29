package com.greedy.mokkoji.api.email.service;

import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final RecruitmentNotificationChannel recruitmentNotificationChannel;
    private final ClubMasterTransferEmailChannel clubMasterTransferEmailChannel;
    private final FavoriteRepository favoriteRepository;

    @Transactional(readOnly = true)
    public void sendRecruitmentNotification(final Long clubId, final String clubName, final Recruitment recruitment) {
        List<Favorite> favorites = favoriteRepository.findByClubIdWithFetchJoin(clubId);

        List<String> userEmails = favorites.stream()
                .map(Favorite::getUser)
                .filter(user -> user != null && user.isEmailOn())
                .map(user -> user.getEmail())
                .filter(email -> email != null && !email.isBlank())
                .toList();

        if (userEmails.isEmpty()) {
            return;
        }

        recruitmentNotificationChannel.sendNotification(
                userEmails, clubId, clubName, recruitment.getRecruitStart(), recruitment.getRecruitEnd()
        );
    }

    public void sendClubMasterTransferNotification(
            final String nextClubMasterEmail,
            final String clubName,
            final String clubMasterTransferLink
    ) {
        if (nextClubMasterEmail == null || nextClubMasterEmail.isBlank()) {
            return;
        }

        clubMasterTransferEmailChannel.sendClubMasterTransferEmail(
                nextClubMasterEmail, clubName, clubMasterTransferLink
        );
    }
}
