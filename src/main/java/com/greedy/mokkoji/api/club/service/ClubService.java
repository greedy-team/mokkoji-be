package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.club.dto.club.ClubDetailResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchResponse;
import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.common.exception.MokkojiException;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import com.greedy.mokkoji.enums.RecruitStatus;
import com.greedy.mokkoji.enums.club.ClubAffiliation;
import com.greedy.mokkoji.enums.club.ClubCategory;
import com.greedy.mokkoji.enums.message.FailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final FavoriteRepository favoriteRepository;

    public ClubDetailResponse findClub(Long userId, Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new MokkojiException(FailMessage.NOT_FOUND_CLUB));
        Recruitment recruitment = recruitmentRepository.findByClubId(club.getId());
        Boolean isFavorite = getIsFavorite(userId, clubId);

        return mapToClubDetailResponse(club, recruitment, isFavorite);
    }

    public ClubSearchResponse findClubsByConditions(final Long userId,
                                                    final String keyword,
                                                    final ClubCategory category,
                                                    final ClubAffiliation affiliation,
                                                    final RecruitStatus status,
                                                    final Pageable pageable) {

        final Page<Club> clubPage = clubRepository.findClubs(keyword, category, affiliation, status, pageable);

        final List<ClubResponse> clubResponses = mapToClubResponses(clubPage.getContent(), userId);
        final PageResponse pageResponse = createPageResponse(clubPage);

        return new ClubSearchResponse(clubResponses, pageResponse);
    }

    private boolean getIsFavorite(Long userId, Long clubId) {
        if (userId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndClubId(userId, clubId);
    }

    private ClubDetailResponse mapToClubDetailResponse(Club club, Recruitment recruitment, Boolean isFavorite) {
        return ClubDetailResponse.of(
                club.getId(),
                club.getName(),
                club.getClubCategory().getDescription(),
                club.getClubAffiliation().getDescription(),
                club.getDescription(),
                recruitment.getRecruitStart(),
                recruitment.getRecruitEnd(),
                club.getLogo(),
                isFavorite,
                club.getInstagram(),
                recruitment.getContent()
        );
    }

    private List<ClubResponse> mapToClubResponses(final Long userId, final List<Club> clubs) {
        return clubs.stream()
                .map(club -> {
                    Recruitment recruitment = recruitmentRepository.findByClubId(club.getId());
                    boolean isFavorite = getIsFavorite(userId, club.getId());
                    return ClubResponse.of(club.getId(),
                            club.getName(),
                            club.getClubCategory().getDescription(),
                            club.getClubAffiliation().getDescription(),
                            club.getDescription(),
                            recruitment.getRecruitStart(),
                            recruitment.getRecruitEnd(),
                            club.getLogo(),
                            isFavorite);
                })
                .collect(Collectors.toList());
    }

    private PageResponse createPageResponse(final Page<Club> clubPage) {
        return PageResponse.of(
                clubPage.getNumber() + 1,
                clubPage.getSize(),
                clubPage.getTotalPages(),
                (int) clubPage.getTotalElements()
        );
    }
}
