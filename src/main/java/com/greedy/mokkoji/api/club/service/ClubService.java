package com.greedy.mokkoji.api.club.service;

import com.greedy.mokkoji.api.club.dto.club.ClubResponse;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchCond;
import com.greedy.mokkoji.api.club.dto.club.ClubSearchResponse;
import com.greedy.mokkoji.api.club.dto.page.PageResponse;
import com.greedy.mokkoji.db.club.entity.Club;
import com.greedy.mokkoji.db.club.repository.ClubRepository;
import com.greedy.mokkoji.db.favorite.repository.FavoriteRepository;
import com.greedy.mokkoji.db.recruitment.entity.Recruitment;
import com.greedy.mokkoji.db.recruitment.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.greedy.mokkoji.api.club.dto.club.ClubResponse.of;
import static com.greedy.mokkoji.api.club.dto.page.PageResponse.of;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final FavoriteRepository favoriteRepository;

    public ClubSearchResponse findClubsByConditions(final Long userId, final ClubSearchCond cond, final Pageable pageable) {
        Page<Club> clubPage = clubRepository.findClubs(cond, pageable);

        Set<Long> favoriteClubIds = getFavoriteClubIds(userId);
        List<ClubResponse> clubResponses = mapToClubResponses(clubPage.getContent(), favoriteClubIds);
        PageResponse pageResponse = createPageResponse(clubPage);

        return new ClubSearchResponse(clubResponses, pageResponse);
    }

    private Set<Long> getFavoriteClubIds(final Long userId) {
        return userId != null ? new HashSet<>(favoriteRepository.findClubIdByUserId(userId)) : Collections.emptySet();
    }

    private List<ClubResponse> mapToClubResponses(final List<Club> clubs, final Set<Long> favoriteClubIds) {
        return clubs.stream()
                .map(club -> {
                    Recruitment recruitment = recruitmentRepository.findByClub(club);
                    boolean isFavorite = favoriteClubIds.contains(club.getId());
                    return of(club.getId(),
                            club.getName(),
                            club.getClubCategory().getDescription(),
                            club.getClubAffiliation().getDescription(),
                            club.getDescription(),
                            recruitment.getRecruitStart().toString(),
                            recruitment.getRecruitEnd().toString(),
                            club.getLogo(),
                            isFavorite);
                })
                .collect(Collectors.toList());
    }

    private PageResponse createPageResponse(final Page<Club> clubPage) {
        return of(clubPage.getNumber() + 1, clubPage.getSize(), clubPage.getTotalPages(), (int) clubPage.getTotalElements());
    }
}
