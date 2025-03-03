package com.greedy.mokkoji.api.favorite.service;

import com.greedy.mokkoji.api.club.dto.club.ClubResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.entity.Favorite;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.db.user.entity.User;
import com.greedy.mokkoji.db.user.repository.UserRepository;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final AppDataS3Client appDataS3Client;

    @Transactional
    public Void addFavorite(final Long userId, final Long clubId) {

        final User user = getUserById(userId);
        final Club club = getClubById(clubId);

        if (favoriteRepository.existsByUserAndClub(user, club)) {
            throw new MokkojiException(FailMessage.CONFLICT_FAVORITE);
        }

        favoriteRepository.save(
                Favorite.builder()
                        .user(user)
                        .club(club)
                        .build()
        );

        return null;
    }

    @Transactional(readOnly = true)
    public List<ClubResponse> findFavoriteClubs(final Long userId) {
        final List<Favorite> favorites = favoriteRepository.findByUserId(userId);

        return favorites.stream()
                .map(favorite -> {
                    final Club club = favorite.getClub();
                    final Recruitment recruitment = recruitmentRepository.findByClubId(club.getId());

                    return ClubResponse.of(
                            club.getId(),
                            club.getName(),
                            club.getClubCategory().getDescription(),
                            club.getClubAffiliation().getDescription(),
                            club.getDescription(),
                            recruitment.getRecruitStart(),
                            recruitment.getRecruitEnd(),
                            appDataS3Client.getPresignedUrl(club.getLogo()),
                            true
                    );
                }).collect(Collectors.toList());
    }

    @Transactional
    public Void deleteFavorite(final Long userId, final Long clubId) {

        final User user = getUserById(userId);
        final Club club = getClubById(clubId);

        if (!favoriteRepository.existsByUserAndClub(user, club)) {
            throw new MokkojiException(FailMessage.NOT_FOUND_FAVORITE);
        }

        favoriteRepository.deleteByUserAndClub(user, club);

        return null;
    }

    private User getUserById(final Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_USER)
        );
    }

    private Club getClubById(final Long clubId) {
        return clubRepository.findById(clubId).orElseThrow(
                () -> new MokkojiException(FailMessage.NOT_FOUND_CLUB)
        );
    }
}
