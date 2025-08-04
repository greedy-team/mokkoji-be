package com.greedy.mokkoji.api.favorite.service;

import com.greedy.mokkoji.api.club.dto.response.ClubResponse;
import com.greedy.mokkoji.api.club.dto.response.ClubsPaginationResponse;
import com.greedy.mokkoji.api.external.AppDataS3Client;
import com.greedy.mokkoji.api.favorite.dto.response.RecruitClubsResponse;
import com.greedy.mokkoji.api.pagination.dto.PageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

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
    public ClubsPaginationResponse findFavoriteClubs(final Long userId, final Pageable pageable) {
        final Page<Favorite> favoritePage = favoriteRepository.findByUserId(userId, pageable);

        final List<Favorite> favorites = favoritePage.getContent();
        List<ClubResponse> clubResponses = favorites.stream()
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
                }).toList();

        final PageResponse pageResponse = createPageResponse(favoritePage);

        return new ClubsPaginationResponse(clubResponses, pageResponse);
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

    @Transactional
    public List<RecruitClubsResponse> getRecruitClubs(final Long userId, final YearMonth yearMonth) {
        List<Long> favoriteClubIds = favoriteRepository.findClubIdsByUserId(userId);

        if (favoriteClubIds.isEmpty()) {
            return null;
        }

        List<Recruitment> recruitments = recruitmentRepository.findByClubIdIn(favoriteClubIds);

        return recruitments.stream()
                .filter(r -> isSameMonth(r, yearMonth))
                .map(r -> RecruitClubsResponse.of(
                        r.getClub().getName(),
                        r.getRecruitStart(),
                        r.getRecruitEnd()
                ))
                .toList();
    }

    private boolean isSameMonth(Recruitment r, YearMonth yearMonth) {
        YearMonth startMonth = YearMonth.from(r.getRecruitStart());
        if (startMonth.equals(yearMonth)) return true;

        YearMonth endMonth = YearMonth.from(r.getRecruitEnd());
        if (endMonth.equals(yearMonth)) return true;

        return false;
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

    private PageResponse createPageResponse(final Page<Favorite> clubPage) {
        return PageResponse.of(
                clubPage.getNumber() + 1,
                clubPage.getSize(),
                clubPage.getTotalPages(),
                (int) clubPage.getTotalElements()
        );
    }
}
